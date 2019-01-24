package io.hektor.core;

import java.util.concurrent.CountDownLatch;

/**
 * @author jonas@jonasborjesson.com
 */
public class DummyActor implements Actor {

    private final CountDownLatch latch;
    private final boolean doReply;

    public static Props props(final CountDownLatch latch) {
        return props(latch, false);
    }

    public static Props props(final CountDownLatch latch, final boolean reply) {
        return Props.forActor(DummyActor.class, () -> new DummyActor(latch, reply));
    }

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
    public void onReceive(final Object msg) {
        final ActorContext context = ctx();
        if (latch != null) {
            latch.countDown();;
        }

        if (doReply) {
            context.sender().tell(msg + " back at you!", context.self());
        }

        // System.out.println("[" + Thread.currentThread().getName() + "] Sender: " + ctx().sender().path() + " Self: " + ctx().self().path() + " Message: " + msg);
        if (context.sender() == sender()) {
            System.out.println("[" + Thread.currentThread().getName() + "] Sender is the same " + sender().path());
        }

        if (context.self() == self()) {
            System.out.println("[" + Thread.currentThread().getName() + "] Self is the same " + self().path());
        }
        // System.out.println("[" + Thread.currentThread().getName() + "] Sender: " + ctx().sender() + " Self: " + ctx().self() + " Message: " + msg);
    }
}
