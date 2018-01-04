package com.liveperson.verticals;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
/**
 * Created by rois on 1/19/16.
 */
public class Verticle extends AbstractVerticle{

    @Override
    public void start(Future<Void> startFuture) {
        System.out.println("Verticle started!");
        vertx.eventBus().consumer("anAddress", message -> {
            System.out.println("1 received message.body() = "
                    + message.body());
        });
    }

    @Override
    public void stop(Future stopFuture) throws Exception {
        System.out.println("Verticle stopped!");
    }
}
