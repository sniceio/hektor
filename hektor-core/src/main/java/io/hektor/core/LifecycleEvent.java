package io.hektor.core;

import io.snice.preconditions.PreConditions;

/**
 * Represents the
 */
public interface LifecycleEvent {

    ActorRef getActor();

    static Terminated terminated(final ActorRef ref) {
        PreConditions.assertNotNull(ref, "You must specify the ActorRef");
        return new Terminated(ref);
    }

    class Terminated implements LifecycleEvent {

        private final ActorRef ref;

        private Terminated(final ActorRef ref) {
            this.ref = ref;
        }

        @Override
        public ActorRef getActor() {
            return ref;
        }
    }

}
