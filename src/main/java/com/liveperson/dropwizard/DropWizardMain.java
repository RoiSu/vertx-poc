package com.liveperson.dropwizard;

import com.liveperson.dropwizard.configuration.AppConf;
import com.liveperson.dropwizard.controller.AuthResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;

/**
 * Created by rois on 6/26/16.
 */
public class DropWizardMain extends Application<AppConf> {

    public static void main(String[] args) throws Exception {

        new DropWizardMain().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppConf> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(AppConf configuration, Environment environment) throws Exception {
        final Client client = new JerseyClientBuilder().build();

        String endpoint = configuration.getEndpoint();
        environment.jersey().register(new AuthResource(client, endpoint));
    }
}
