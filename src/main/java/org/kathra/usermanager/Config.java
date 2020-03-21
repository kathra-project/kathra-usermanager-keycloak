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

package org.kathra.usermanager;

import org.kathra.utils.ConfigManager;

/**
 * @author Jérémy Guillemot <Jeremy.Guillemot@kathra.org>
 */
public class Config extends ConfigManager {

    private String keycloakAuthUrl;
    private String keycloakRealm;
    private String keycloakRealmAdmin;
    private String keycloakClientId;
    private String keycloakUsername;
    private String keycloakPassword;

    public Config() {
        keycloakAuthUrl = getProperty("KEYCLOAK_ADMIN_AUTH_URL");
        keycloakRealmAdmin = getProperty("KEYCLOAK_ADMIN_REALM");
        keycloakRealm = getProperty("KEYCLOAK_REALM");
        keycloakClientId = getProperty("KEYCLOAK_ADMIN_CLIENT_ID");
        keycloakUsername = getProperty("KEYCLOAK_ADMIN_USERNAME");
        keycloakPassword = getProperty("KEYCLOAK_ADMIN_PASSWORD");
    }

    public String getKeycloakAuthUrl() {
        return keycloakAuthUrl;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public String getKeycloakClientId() {
        return keycloakClientId;
    }


    public String getKeycloakUsername() {
        return keycloakUsername;
    }

    public String getKeycloakPassword() {
        return keycloakPassword;
    }

    public String getKeycloakRealmAdmin() {
        return keycloakRealmAdmin;
    }
}
