package io.hektor.core.internal.messages;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Internal message for indicating that an actor has fully stopped. This message
 * is only passed to a parent, which is why the only thing stored in this message
 * is the name of the child. The parent will know all its children by name.
 *
 * @author jonas@jonasborjesson.com
 */
public class Stopped {
    private final String name;

    private Stopped(final String name) {
        this.name = name;
    }

    public String child() {
        return name;
    }

    public static Stopped create(final String name) {
        assertNotNull(name);
        return new Stopped(name);
    }
}
