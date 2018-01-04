package com.liveperson.verticals;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * Created by rois on 1/19/16.
 */
public class EventBusReceiverVerticle extends AbstractVerticle {

    private String name = null;

    public EventBusReceiverVerticle(String name) {
        this.name = name;
    }

    public void start(Future<Void> startFuture) {
        vertx.eventBus().consumer("anAddress", message -> {
            System.out.println(this.name +
                    " received message: " +
                    message.body());
            vertx.eventBus().send   ("anAddressReply", "reply message 1");
        });
    }
}
