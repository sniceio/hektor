package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Dispatcher;

import java.util.Optional;

/**
 * An internal extension to the Dispatcher
 *
 * @author jonas@jonasborjesson.com
 */
public interface InternalDispatcher extends Dispatcher {

    /**
     * Register an actor with this dispatcher, which should only be done once
     * as soon as the Actor has been created.
     *
     */
    void register(ActorRef ref, Actor actor);

    void unregister(ActorRef ref);

    Optional<ActorBox> lookup(ActorRef ref);

    Optional<ActorBox> lookup(ActorPath path);

    /**
     * Lookup the reference of an actor based on its absolute path expressed as a string.
     *
     * @param path
     * @return
     * @throws IllegalArgumentException in case the supplied path is not an absolute path
     */
    Optional<ActorBox> lookup(String path) throws IllegalArgumentException;
}
