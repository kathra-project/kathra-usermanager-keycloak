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

import org.kathra.core.model.Group;
import org.kathra.usermanager.services.KeycloakService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
/**
 * @author Jérémy Guillemot <Jeremy.Guillemot@kathra.org>
 */
public class UserManagerControllerTest {

    public static final String GROUP_ID = "groupId";
    public static final String GROUP = "group";
    public static final String GROUP_PATH = "rootGroup/group";
    public static final String SUBGROUP_ID = "subGroupId";
    public static final String SUBGROUP = "subGroup";
    public static final String SUBGROUP_PATH = "rootGroup/group/subGroup";

    private UserManagerController underTest;
    static List<GroupRepresentation> keycloakGroups = new ArrayList();
    static List<UserRepresentation> keycloakGroupUsers = new ArrayList();

    @Mock
    private KeycloakService keycloakService;

    @BeforeAll
    static void setUp() {
        List<GroupRepresentation> subGroups = new ArrayList();
        GroupRepresentation subGroup = new GroupRepresentation();
        GroupRepresentation group = new GroupRepresentation();
        subGroup.setId(SUBGROUP_ID);
        subGroup.setName(SUBGROUP);
        subGroup.setPath(SUBGROUP_PATH);
        subGroups.add(subGroup);
        group.setId(GROUP_ID);
        group.setName(GROUP);
        group.setPath("rootGroup/group");
        group.setSubGroups(subGroups);

        UserRepresentation user = new UserRepresentation();
        user.setId("userId");
        user.setUsername("firstname.lastname");
        keycloakGroupUsers.add(user);
        keycloakGroups.add(group);
    }

    @BeforeEach
    void setUpEach() throws Exception {
        keycloakService = Mockito.mock(KeycloakService.class);
        Mockito.reset(keycloakService);
        Mockito.when(keycloakService.getGroups()).thenReturn(keycloakGroups);
        Mockito.when(keycloakService.getGroupMembers("subGroupId")).thenReturn(keycloakGroupUsers);
        underTest = new UserManagerController(keycloakService);
    }

    @Test
    public void given_nominal_args_when_getGroups_then_works() throws Exception {
        List<Group> groups = underTest.getGroups();
        Assertions.assertEquals(2, groups.size(), "Number of returned groups");
        Group group = groups.get(0);
//        Assertions.assertEquals(group.getId(), GROUP_ID);
        Assertions.assertEquals(group.getName(), GROUP);
        Assertions.assertEquals(group.getPath(), GROUP_PATH);
        Group subGroup = groups.get(1);
//        Assertions.assertEquals(subGroup.getId(), SUBGROUP_ID);
        Assertions.assertEquals(subGroup.getName(), SUBGROUP);
        Assertions.assertEquals(subGroup.getPath(), SUBGROUP_PATH);
    }
}