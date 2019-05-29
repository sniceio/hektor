package io.hektor.core.internal;

import io.hektor.core.ActorPath;
import io.snice.preconditions.PreConditions;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotEmpty;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultActorPath implements ActorPath {

    public static final ActorPath NoPath = new DefaultActorPath(null, "none");

    private final static int prime = 31;

    private final Optional<ActorPath> parent;
    private final String name;
    private final int hashCode;

    public DefaultActorPath(final ActorPath parent, final String name) {
        this.parent = Optional.ofNullable(parent);
        this.name = name;
        hashCode = this.parent.isPresent() ?
                (prime + parent.hashCode()) * prime + name.hashCode() :
                prime + name.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Create an actor path based on an absolute path.
     *
     * @param path
     * @return
     */
    public static ActorPath create(final ActorPath parent, final String path) {
        assertNotEmpty(path, "The path cannot be null or the empty string");
        ActorPath current = parent;
        int start = 0;
        int stop = 0;
        while(stop < path.length()) {
            if (path.charAt(stop) == '/') {
                if (stop > 0) {
                    final String name = path.substring(start, stop);
                    if (current == null) {
                        current = new DefaultActorPath(null, name);
                    } else {
                        if (isRelative(name)) {
                            final Optional<ActorPath> optional = current.parent();
                            if (optional.isPresent()) {
                                current = optional.get();
                            } else {
                                // TODO: i guess we should just keep returning the root or something
                                throw new RuntimeException("havent implemented this yet.");
                            }
                        } else if (isCurrentPath(name)) {
                            // just consume it
                        } else {
                            current = current.createChild(name);
                        }
                    }
                }
                start = stop + 1;
            }
            ++stop;
        }
        if (stop > start) {
            if (current == null) {
                current = new DefaultActorPath(null, path.substring(start, stop));
            } else {
                current = current.createChild(path.substring(start, stop));
            }
        }
        return current;
    }

    private static boolean isRelative(final String path) {
        return path.length() == 2 && path.charAt(0) == '.' && path.charAt(1) == '.';
    }

    private static boolean isCurrentPath(final String path) {
        return path.length() == 1 && path.charAt(0) == '.';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        try {
            if (this == obj) {
                return true;
            }

            final DefaultActorPath other = (DefaultActorPath) obj;
            if (!name.equals(other.name)) {
                return false;
            }

            if (parent.isPresent() && other.parent.isPresent()) {
                return parent.get().equals(other.parent.get());
            }

            return parent.isPresent() == other.parent.isPresent();
        } catch (final ClassCastException e) {
            return false;
        }
    }

    @Override
    public Optional<ActorPath> parent() {
        return parent;
    }

    @Override
    public String name() {
        return name;
    }

    public String toString() {
        return (parent.isPresent() ? parent.get().toString() + "/" : "/" ) + name;
    }
}
