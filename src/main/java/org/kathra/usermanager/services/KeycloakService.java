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

import com.google.common.collect.ImmutableList;
import org.kathra.core.model.Group;
import org.kathra.core.model.User;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Group> getMemberGroups(User user) {
        return kathraRealm.users().get(getUserRepresentation(user).getId()).groups().stream().map(groupR -> new Group()
                .id(groupR.getId())
                .name(groupR.getName())
                .path(groupR.getPath())).collect(Collectors.toList());
    }


    public List<GroupRepresentation> getGroups() {
        GroupResource kathraRootGroup = kathraRealm.groups().group(kathraRealm.getGroupByPath("kathra-projects").getId());
        return kathraRootGroup.toRepresentation().getSubGroups();
    }

    public User createUser(User user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setType("password");
        passwordCred.setValue(user.getPassword());
        userRepresentation.setCredentials(ImmutableList.of(passwordCred));
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setUsername(user.getName());
        Response response = kathraRealm.users().create(userRepresentation);

        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        //user.setProvider("Keycloak");
        //user.setProviderId(userId);
        return user;
    }

    public void addUserToGroup(User user, Group group) {
        UserRepresentation userR = kathraRealm.users().list().stream().filter(u -> u.getUsername().equals(user.getName())).findFirst().get();
        GroupRepresentation groupR = kathraRealm.groups().groups().stream().filter(u -> u.getName().equals(group.getName())).findFirst().get();
        kathraRealm.users().get(userR.getId()).joinGroup(groupR.getId());
        return;
    }
    public void removeUserToGroup(User user, Group group) {
        return;
    }

    public Group createGroup(Group group) {
        GroupRepresentation groupRepresentation = new GroupRepresentation();
        groupRepresentation.setName(group.getName());
        groupRepresentation.setPath(group.getPath());
        Response response = kathraRealm.groups().add(groupRepresentation);
        String groupId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        //group.providerId(groupId);

        return group;
    }

    public User getUser(String userId) {
        UserRepresentation userR = kathraRealm.users().list().stream().filter(u -> u.getUsername().equals(userId)).findFirst().get();
        return new User().name(userR.getUsername()).lastName(userR.getLastName()).firstName(userR.getFirstName()).email(userR.getEmail());
    }


    private UserRepresentation getUserRepresentation(User user) {
        return kathraRealm.users().list().stream().filter(u -> u.getUsername().equals(user.getName())).findFirst().get();
    }

}
