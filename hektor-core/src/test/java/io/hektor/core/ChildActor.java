package io.hektor.core;

import java.util.concurrent.CountDownLatch;

/**
 * @author jonas@jonasborjesson.com
 */
public class ChildActor implements Actor {

    private final CountDownLatch latch;

    public ChildActor(final CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onReceive(ActorContext context, Object msg) {
        System.err.println("yes I recevied a message! " + msg);
        latch.countDown();
    }
}
