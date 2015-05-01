package io.hektor.core.internal;

import io.hektor.core.ActorPath;

import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultActorPath implements ActorPath {

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
