package io.hektor.core.internal;

import io.hektor.core.ActorRef;

import java.util.concurrent.CompletableFuture;

/**
 * @author jonas@jonasborjesson.com
 */
public class Envelope {

    private final ActorRef sender;
    private final ActorRef receiver;
    private final Object msg;
    private final Priority priority;

    /**
     * If present, then this is an "ask" as opposed to a tell
     */
    private final CompletableFuture<Object> askFuture;

    public ActorRef getSender() {
        return sender;
    }

    public Envelope(final ActorRef sender, final ActorRef receiver, final Object msg) {
        this(Priority.NORMAL, sender, receiver, msg);
    }

    public Envelope(final Priority priority, final ActorRef sender, final ActorRef receiver, final Object msg) {
        this(Priority.NORMAL, sender, receiver, msg, null);
    }

    public Envelope(final Priority priority, final ActorRef sender, final ActorRef receiver, final Object msg, final CompletableFuture<Object> askFuture) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.priority = priority;
        this.askFuture = askFuture;
    }

    public boolean isAsk() {
        return askFuture != null;
    }

    public CompletableFuture<Object> askFuture() {
        return askFuture;
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

    public Priority priority() {
        return priority;
    }
}
