package io.hektor.core;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Dispatcher {

    /**
     * Dispatch a message from the sender to the receiver.
     *
     * @param sender
     * @param receiver
     * @param msg
     */
    void dispatch(ActorRef sender, ActorRef receiver, Object msg);
}
