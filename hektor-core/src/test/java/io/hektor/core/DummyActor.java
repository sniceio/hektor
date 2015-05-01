package io.hektor.core;

/**
 * @author jonas@jonasborjesson.com
 */
public class DummyActor implements Actor {

    public DummyActor() {

    }

    @Override
    public ActorRef self() {
        return null;
    }

    @Override
    public ActorRef sender() {
        return null;
    }

    @Override
    public ActorContext context() {
        return null;
    }

    @Override
    public void onReceive(final Object msg) {
        System.err.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}
