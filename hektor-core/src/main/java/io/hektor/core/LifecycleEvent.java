package io.hektor.core;

import io.snice.preconditions.PreConditions;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Represents the
 */
public interface LifecycleEvent {

    ActorPath getActor();

    default boolean isActor(final ActorPath path) {
        assertNotNull(path, "The path to compare with cannot be null");
        return getActor().equals(path);
    }

    default boolean isActor(final ActorRef ref) {
        assertNotNull(ref, "The ActorRef to compare with cannot be null");
        return getActor().equals(ref.path());
    }

    static Terminated terminated(final ActorPath path) {
        assertNotNull(path, "You must specify the ActorPath");
        return new Terminated(path);
    }

    class Terminated implements LifecycleEvent {

        private final ActorPath path;

        private Terminated(final ActorPath path) {
            this.path = path;
        }

        @Override
        public ActorPath getActor() {
            return path;
        }

        @Override
        public String toString() {
            return String.format("%s.%s{actor: %s}", LifecycleEvent.class.getSimpleName(), Terminated.class.getSimpleName(), path);
        }
    }

}
