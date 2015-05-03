package io.hektor.core.internal;

import io.hektor.core.ActorRef;

/**
 * @author jonas@jonasborjesson.com
 */
public class Envelope {

    private final ActorRef sender;
    private final ActorRef receiver;
    private final Object msg;

    public ActorRef getSender() {
        return sender;
    }

    public Envelope(final ActorRef sender, final ActorRef receiver, final Object msg) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;

    }

    public ActorRef sender() {
        return sender;
    }

    public ActorRef receiver() {
        return receiver;
    }

    public Object message() {
        return msg;
    }
}
