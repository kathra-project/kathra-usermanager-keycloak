/* 
 * Copyright 2019 The Kathra Authors.
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
 *
 *    IRT SystemX (https://www.kathra.org/)    
 *
 */

package org.kathra.usermanager.services;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * @author Jérémy Guillemot <Jeremy.Guillemot@kathra.org>
 */
public class KeycloakService {

    private Keycloak keycloak;
    RealmResource kathraRealm;

    public KeycloakService(String url, String realm, String clientId, String clientSecret) {
        this.keycloak = KeycloakBuilder.builder() //
                .serverUrl(url) //
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
                .realm(realm) //
                .clientId(clientId) //
                .clientSecret(clientSecret) //
                .build();
        kathraRealm = keycloak.realm(realm);
    }

    public List<UserRepresentation> getGroupMembers(String groupId) {
        GroupResource groupResource = kathraRealm.groups().group(groupId);
        return groupResource.members(0, 1000);
    }

    public List<GroupRepresentation> getGroups() {
        GroupResource kathraRootGroup = kathraRealm.groups().group(kathraRealm.getGroupByPath("kathra-projects").getId());
        return kathraRootGroup.toRepresentation().getSubGroups();
    }
}
