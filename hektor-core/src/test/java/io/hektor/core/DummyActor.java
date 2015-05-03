package io.hektor.core;

import java.util.concurrent.CountDownLatch;

/**
 * @author jonas@jonasborjesson.com
 */
public class DummyActor implements Actor {

    private final CountDownLatch latch;
    private final boolean doReply;

    public DummyActor() {
        this(null);
    }

    public DummyActor(final CountDownLatch latch) {
        this(latch, false);
    }

    public DummyActor(final CountDownLatch latch, final Boolean doReply) {
        this.latch = latch;
        this.doReply = doReply;
    }

    @Override
    public void onReceive(final ActorContext context, final Object msg) {
        if (latch != null) {
            latch.countDown();;
        }

        if (doReply) {
            context.sender().tell(msg + " back at you!", context.self());
        }

        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}
