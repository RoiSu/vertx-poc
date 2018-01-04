package com.liveperson.verticals;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

/**
 * Created by rois on 1/19/16.
 */
public class VertxHttpServerVerticle extends AbstractVerticle {

    private HttpServer httpServer = null;

    @Override
    public void start() throws Exception {
        httpServer = vertx.createHttpServer();

        httpServer.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                System.out.println("incoming request!");
                HttpServerResponse response = request.response();
                response.write("Vert.x is alive!");
                response.end();
            }
        });

        httpServer.listen(9999);
    }
}
