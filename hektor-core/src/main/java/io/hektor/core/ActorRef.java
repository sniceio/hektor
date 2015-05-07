package io.hektor.core;

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

    void tell(Object msg, ActorRef sender);

    /**
     * Send a message to the actor but specify no sender.
     *
     * @param msg
     */
    void tellAnonymously(Object msg);

    class NoneActorRef implements ActorRef {

        private static final ActorRef NONE = new NoneActorRef();

        private NoneActorRef() {
            // left empty intentionally. Just so that no one
            // can create an instance of this class
        }

        @Override
        public ActorPath path() {
            return null;
        }

        @Override
        public void tell(Object msg, ActorRef sender) {
            // ignored
        }

        @Override
        public void tellAnonymously(Object msg) {
            // ignored.
        }

        @Override
        public String toString() {
            return "NONE";
        }
    }

}
