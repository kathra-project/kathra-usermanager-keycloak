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
package org.kathra.usermanager.controller;

import org.kathra.core.model.Assignation;
import org.kathra.core.model.Group;
import org.kathra.core.model.User;
import org.kathra.usermanager.Config;
import org.kathra.usermanager.service.UserManagerService;
import org.kathra.usermanager.services.KeycloakService;
import org.apache.camel.cdi.ContextName;
import org.kathra.utils.KathraException;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named("UserManagerController")
@ContextName("UserManager")
public class UserManagerController implements UserManagerService {

    private Config config;
    KeycloakService keycloakService;

    public UserManagerController() {
        config = new Config();
        keycloakService = new KeycloakService(
                config.getKeycloakAuthUrl(),
                config.getKeycloakRealmAdmin(),
                config.getKeycloakRealm(),
                config.getKeycloakClientId(),
                config.getKeycloakUsername(),
                config.getKeycloakPassword());
    }
    public UserManagerController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    private void populateGroup(List<Group> groups, List<GroupRepresentation> subGroups) {
        subGroups.forEach(subGroupRepresentation -> {
                    Group group = new Group()
                            //.id(subGroupRepresentation.getId())
                            .name(subGroupRepresentation.getName())
                            .path(subGroupRepresentation.getPath());

                    List<UserRepresentation> members = keycloakService.getGroupMembers(group.getId());
                    for (UserRepresentation member : members) {
                        Assignation userAssignation = new Assignation().name(member.getUsername());
                        if (group.getMembers() == null || !group.getMembers().contains(userAssignation)) {
                            group.addMembersItem(userAssignation);
                        }
                    }
                    groups.add(group);
                    if (subGroupRepresentation.getSubGroups() != null)
                        populateGroup(groups, subGroupRepresentation.getSubGroups());
                }
        );
    }

    @Override
    public User assignUserToGroup(String userId, String groupPath) throws Exception {
        keycloakService.addUserToGroup(getUser(userId), getGroup(groupPath));
        return null;
    }

    /**
     * Create a new group
     *
     * @return Group
     */
    public Group createGroup(Group group) throws Exception {
        return keycloakService.createGroup(group);
    }

    /**
     * Create a new user
     *
     * @return User
     */
    public User createUser(User user) throws Exception {
        return keycloakService.createUser(user);
    }

    @Override
    public Group deleteGroup(String groupPath) throws Exception {
        return null;
    }

    @Override
    public User deleteUser(String userId) throws Exception {
        return null;
    }

    /**
     * Return group object
     *
     * @param groupPath Group Path (required)
     * @return Group
     */
    public Group getGroup(String groupPath) throws Exception {
        return getGroups().stream().filter(g -> g.getPath().equals(groupPath)).findFirst().orElseThrow( () -> new KathraException("Group not found", null, KathraException.ErrorCode.NOT_FOUND ));
    }

    /**
     * Find all groups
     *
     * @return List<Group>
     */
    public List<Group> getGroups() throws Exception {
        List<Group> groups = new ArrayList();
        List<GroupRepresentation> subGroups = keycloakService.getGroups();
        populateGroup(groups, subGroups);
        return groups;
    }

    @Override
    public List<Group> getGroupsAssignationsFromUser(String userId) throws Exception {
        return keycloakService.getMemberGroups(getUser(userId));
    }

    /**
     * Return user object
     *
     * @param userId User id (required)
     * @return User
     */
    public User getUser(String userId) throws Exception {
        return keycloakService.getUser(userId);
    }

    /**
     * Find all users
     *
     * @return List<User>
     */
    public List<User> getUsers() throws Exception {
        return keycloakService.getUsers();
    }

    @Override
    public Group patchGroup(String groupPath, Group group) throws Exception {
        return null;
    }

    @Override
    public User patchUser(String userId, User user) throws Exception {
        return null;
    }

    @Override
    public User unassignUserToGroup(String userId, String groupPath) throws Exception {
        keycloakService.removeUserToGroup(getUser(userId), getGroup(groupPath));
        return null;
    }

    @Override
    public Group updateGroup(String groupPath, Group group) throws Exception {
        return null;
    }

    @Override
    public User updateUser(String userId, User user) throws Exception {
        return null;
    }
}
