package com.liveperson.dropwizard.configuration;

import io.dropwizard.Configuration;

/**
 * Created by rois on 6/26/16.
 */
public class AppConf extends Configuration {

    String endpoint = "http://localhost:9002/testDW/parsedJwtDW";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
