package com.liveperson.verticals;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * Created by rois on 1/19/16.
 */
public class EventBusSenderVerticle extends AbstractVerticle {

    public void start(Future<Void> startFuture) {
        vertx.eventBus().publish("anAddress", "message 2");
        vertx.eventBus().send   ("anAddress", "message 1");
    }
}
