package io.hektor.core;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Actor {

    /**
     * Obtain the @ActorRef pointing to yourself.
     *
     * @return
     */
    ActorRef self();

    /**
     * Obtain the @ActorRef pointing to the sender of the message
     * that is currently being processed.
     *
     * @return
     */
    ActorRef sender();

    /**
     *
     * @return
     */
    ActorContext context();

    void onReceive(Object msg);
}
