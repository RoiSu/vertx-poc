package com.liveperson;

import com.liveperson.verticals.EventBusReceiverVerticle;
import com.liveperson.verticals.EventBusSenderVerticle;
import com.liveperson.verticals.Verticle;
import io.vertx.core.Vertx;

/**
 * Created by rois on 1/19/16.
 */
public class VertxApp2 {

    public static void main(String[] args) throws InterruptedException {
        //Vertx vertx = Vertx.vertx();
        //vertx.deployVerticle(new Verticle());

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new EventBusReceiverVerticle("R1"));
        vertx.deployVerticle(new EventBusReceiverVerticle("R2"));

        Thread.sleep(3000);
        vertx.deployVerticle(new EventBusSenderVerticle());
    }
}
