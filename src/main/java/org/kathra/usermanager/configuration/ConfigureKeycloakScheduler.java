package org.kathra.usermanager.configuration;

import org.apache.camel.builder.RouteBuilder;
import org.kathra.usermanager.services.PermissionsServices;

public class ConfigureKeycloakScheduler extends RouteBuilder {

    @Override
    public void configure() {
        Config config = new Config();
        from("scheduler://foo?delay="+config.getJobVerifyPermission()).process(PermissionsServices.getInstance()).to("mock:success");
    }

}