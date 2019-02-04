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

    void tell(Object msg, ActorRef sender);

    /**
     * Convenience method for talking to yourself :-)
     *
     * @param msg
     */
    default void tell(Object msg) {
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
        public CompletionStage<Object> ask(Object msg, ActorRef sender) {
            return NO_FUTURE;
        }

        @Override
        public void tell(Object msg, ActorRef sender) {
            // ignored
        }

        @Override
        public void tell(Priority priority, Object msg, ActorRef sender) {
            // ignored
        }

        @Override
        public void tellAnonymously(Object msg) {
            // ignored.
        }

        @Override
        public void monitor(ActorRef ref) {
            // ignored.
        }

        @Override
        public String toString() {
            return "NONE";
        }
    }

}
