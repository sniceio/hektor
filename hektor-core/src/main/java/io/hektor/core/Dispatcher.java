package io.hektor.core;

import java.util.concurrent.CompletableFuture;

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

    /**
     *
     * @param sender
     * @param receiver
     * @param msg
     * @return
     * @throws IllegalArgumentException in case the msg is null
     */
    CompletableFuture<Object> ask(ActorRef sender, ActorRef receiver, Object msg) throws IllegalArgumentException;
}
