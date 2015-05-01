package io.hektor.core;

import io.hektor.core.internal.DefaultActorPath;

import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ActorPath {

    /**
     * The parent that may be non-existing in which case this is a root
     * path.
     *
     * @return
     */
    Optional<ActorPath> parent();

    String name();

    default boolean isRoot() {
        return !parent().isPresent();
    }

    default ActorPath createChild(final String name) {
        return new DefaultActorPath(this, name);
    }

}
