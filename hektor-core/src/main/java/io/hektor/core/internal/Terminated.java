package io.hektor.core.internal;

import io.hektor.core.ActorPath;

import static io.hektor.core.internal.PreConditions.assertNotNull;

/**
 * A message indicating that the actor identified by the path has been terminated.
 *
 * @author jonas@jonasborjesson.com
 */
public final class Terminated {

    private final ActorPath path;

    public static Terminated of(final ActorPath path) {
        assertNotNull(path);
        return new Terminated(path);
    }

    private Terminated(final ActorPath path) {
        this.path = path;
    }

    public ActorPath actor() {
        return path;
    }
}
