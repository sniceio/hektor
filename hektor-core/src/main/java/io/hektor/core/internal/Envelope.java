package io.hektor.core.internal;

import io.hektor.core.ActorRef;
import io.snice.protocol.Request;

import java.util.concurrent.CompletableFuture;

/**
 * @author jonas@jonasborjesson.com
 */
public class Envelope {

    private final ActorRef sender;
    private final ActorRef receiver;
    private final Object msg;
    private final Request request;
    private final DefaultResponse response;
    private final Priority priority;

    /**
     * If present, then this is an "ask" as opposed to a tell
     */
    private final CompletableFuture<Object> askFuture;

    public Envelope(final ActorRef sender, final ActorRef receiver, final Object msg) {
        this(Priority.NORMAL, sender, receiver, msg);
    }

    public Envelope(final ActorRef sender, final ActorRef receiver, final Request request) {
        this(Priority.NORMAL, sender, receiver, ((DefaultRequest)request).getMessage(), null, request, null);
    }

    public Envelope(final ActorRef sender, final ActorRef receiver, final DefaultResponse response) {
        this(Priority.NORMAL, sender, receiver, response.getMessage(), null, null, response);
    }

    public Envelope(final Priority priority, final ActorRef sender, final ActorRef receiver, final Object msg) {
        this(Priority.NORMAL, sender, receiver, msg, null, null, null);
    }

    public Envelope(final Priority priority, final ActorRef sender, final ActorRef receiver, final Object msg,
                    final CompletableFuture<Object> askFuture, final Request request,
                    final DefaultResponse response) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.priority = priority;
        this.askFuture = askFuture;
        this.request = request;
        this.response = response;
    }

    public boolean isResponse() {
        return response != null;
    }

    public boolean isRequest() {
        return request != null;
    }

    public Request getRequest() {
        return request;
    }

    public DefaultResponse getResponse() {
        return response;
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
