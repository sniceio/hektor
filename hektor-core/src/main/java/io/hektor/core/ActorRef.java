package io.hektor.core;

import io.hektor.core.internal.Priority;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ActorRef {

    static ActorRef None() {
        return NoneActorRef.NONE;
    }

    /**
     * The unique path to this actor.
     *
     * @return
     */
    ActorPath path();

    /**
     * Ask the the {@link Actor} something.
     *
     * @param msg
     * @param sender
     * @return
     * @throws IllegalArgumentException in case the sender is not specified.
     */
    CompletionStage<Object> ask(Object msg, ActorRef sender) throws IllegalArgumentException;

    default CompletionStage<Object> ask(Object msg) {
        return ask(msg, this);
    }

    /**
     * If you request information from another {@link Actor}, you are expecting a response and you want to
     * track this interaction.
     *
     * @param msg
     * @param sender
     * @return
     */
    Request request(Object msg, ActorRef sender);


    /**
     * When you respond to a {@link Request}, you must do so via this method, which will ensure that
     * the underlying transaction is still valid and pass the {@link Response} to the original {@link Actor}.
     *
     * @param msg the messege that serves as the actual response to the sender.
     * @param req the original request, which resulted in the given message to be sent back.
     * @param isFinal flag indicating if this is the last and final response to the original request.
     * @return a {@link Response} that represents the message that is sent to the original sender.
     */
    Response respond(Object msg, Request req, ActorRef sender, boolean isFinal);

    /**
     * Convenience method for {@link #respond(Object, Request, ActorRef, boolean)} where isFinal is set to true.
     */
    default Response respond(final Object msg, final Request req, final ActorRef sender) {
        return respond(msg, req, sender, true);
    }

    void tell(Object msg, ActorRef sender);

    /**
     * Convenience method for talking to yourself :-)
     *
     * @param msg
     */
    default void tell(final Object msg) {
        tell(msg, this);
    }

    void tell(Priority priority, Object msg, ActorRef sender);

    /**
     * Send a message to the actor but specify no sender.
     *
     * @param msg
     */
    void tellAnonymously(Object msg);

    /**
     * Monitor a particular {@link Actor}, meaning to get all the lifecycle events
     * from the actor. If the {@link Actor}, as represented by the given {@link ActorRef} does not
     * exist, a
     *
     * @param ref
     */
    void monitor(ActorRef ref);

    class NoneActorRef implements ActorRef {

        private static final ActorRef NONE = new NoneActorRef();

        /**
         * There simply is no future with this actor!
         */
        private static final CompletableFuture<Object> NO_FUTURE = new CompletableFuture<>();
        static {
            NO_FUTURE.completeExceptionally(new IllegalStateException("You cannot ask me anything because I' a None Actor Ref!"));
        }

        private NoneActorRef() {
            // left empty intentionally. Just so that no one
            // can create an instance of this class
        }

        @Override
        public ActorPath path() {
            return null;
        }

        @Override
        public CompletionStage<Object> ask(final Object msg, final ActorRef sender) {
            return NO_FUTURE;
        }

        @Override
        public Request request(final Object msg, final ActorRef sender) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public Response respond(final Object msg, final Request req, final ActorRef sender, final boolean isFinal) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public void tell(final Object msg, final ActorRef sender) {
            // ignored
        }

        @Override
        public void tell(final Priority priority, final Object msg, final ActorRef sender) {
            // ignored
        }

        @Override
        public void tellAnonymously(final Object msg) {
            // ignored.
        }

        @Override
        public void monitor(final ActorRef ref) {
            // ignored.
        }

        @Override
        public String toString() {
            return "NONE";
        }
    }

}
