package io.hektor.core;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ActorRef {

    /**
     * The unique path to this actor.
     *
     * @return
     */
    ActorPath path();

    void tell(Object msg, ActorRef sender);

    /**
     * Send a message to the actor but specify no sender.
     *
     * @param msg
     */
    void tellAnonymously(Object msg);
}
