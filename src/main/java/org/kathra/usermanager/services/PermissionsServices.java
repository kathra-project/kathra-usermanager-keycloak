/*
 * Copyright (c) 2020. The Kathra Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *    IRT SystemX (https://www.kathra.org/)
 *
 */

package org.kathra.usermanager.services;

import com.google.common.io.Resources;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.kathra.core.model.Component;
import org.kathra.core.model.Implementation;
import org.kathra.usermanager.configuration.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.authorization.*;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class PermissionsServices implements Processor {

    private Logger logger = Logger.getLogger(PermissionsServices.class.getName());
    private static PermissionsServices instance;
    private Keycloak keycloak;
    private String realmName;
    private String clientName;

    private final String[] resources = {
            Component.class.getSimpleName().toLowerCase(),
            Implementation.class.getSimpleName().toLowerCase()
    };

    public PermissionsServices(Keycloak keycloak, String realmName, String clientName) {
        this.keycloak = keycloak;
        this.realmName = realmName;
        this.clientName = clientName;
    }

    public static PermissionsServices getInstance() {
        if (instance == null) {
            Config config = new Config();
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(config.getKeycloakAuthUrl())
                    .grantType(OAuth2Constants.PASSWORD)
                    .realm(config.getKeycloakRealmAdmin())
                    .clientId(config.getKeycloakAdminClientId())
                    .username(config.getKeycloakAdminUsername())
                    .password(config.getKeycloakAdminPassword())
                    .build();
            instance = new PermissionsServices(keycloak, config.getKeycloakRealm(), config.getKeycloakClientId());
        }
        return instance;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.info("Start sync keycloak permission");
        ClientRepresentation client = getClient(clientName);
        JSPolicyRepresentation policy = initPolicy(client, "OnlyUsers", "Only users from shared group policy");

        for (String resource:resources) {
            for (Scope access:Scope.values()) {
                String scopeName = "kathra:scope:"+resource+":"+access.name().toLowerCase();
                ScopeRepresentation scope = initClientScope(client, scopeName);
                initPermissionScopeBased("Only users from shared group can "+access+" "+resource, client, scope, policy);
            }
        }
    }

    private ClientRepresentation getClient(String clientName) {
        return keycloak.realm(realmName).clients().findAll().stream().filter(clientRepresentation -> clientRepresentation.getName().equals(clientName)).findFirst().get();
    }
    private ClientResource getClientResource(ClientRepresentation client) {
        return keycloak.realm(realmName).clients().get(client.getId());
    }

    private JSPolicyRepresentation initPolicy(ClientRepresentation client, String policyName, String description) throws IOException {
        logger.info("Init policy " + policyName);
        try {
            return getClientResource(client).authorization().policies().js().findByName(policyName);
        } catch (NotFoundException e) {}
        JSPolicyRepresentation policyRepresentation = new JSPolicyRepresentation();
        policyRepresentation.setName(policyName);
        policyRepresentation.setDescription(description);
        policyRepresentation.setLogic(Logic.POSITIVE);
        policyRepresentation.setCode(Resources.toString(this.getClass().getResource("/policy.js"), StandardCharsets.UTF_8));
        getClientResource(client).authorization().policies().js().create(policyRepresentation);
        return getClientResource(client).authorization().policies().js().findByName(policyName);
    }



    private ScopeRepresentation initClientScope(ClientRepresentation client,  String clientScopeName) {
        logger.info("Init client scope " + clientScopeName);
        try {
            return getClientResource(client).authorization().scopes().scopes().stream().filter(i -> i.getName().equals(clientScopeName)).findFirst().orElseThrow(() -> new NotFoundException());
        } catch(NotFoundException e) {}
        ScopeRepresentation scope = new ScopeRepresentation();
        scope.setName(clientScopeName);
        getClientResource(client).authorization().scopes().create(scope);
        return getClientResource(client).authorization().scopes().scopes().stream().filter(i -> i.getName().equals(clientScopeName)).findFirst().orElseThrow(() -> new NotFoundException());
    }

    private ScopePermissionRepresentation initPermissionScopeBased(String name, ClientRepresentation client, ScopeRepresentation scope, JSPolicyRepresentation policy) {
        logger.info("Init client permission scope based " + name);
        try {
            return getClientResource(client).authorization().permissions().scope().findByName(name);
        } catch(NotFoundException e) {}
        ScopePermissionRepresentation permission = new ScopePermissionRepresentation();
        permission.setName(name);
        permission.setLogic(Logic.POSITIVE);
        permission.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        permission.addScope(scope.getId());
        permission.addPolicy(policy.getId());
        getClientResource(client).authorization().permissions().scope().create(permission);
        return getClientResource(client).authorization().permissions().scope().findByName(name);
    }

}
