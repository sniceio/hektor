package io.hektor.core.internal;

import io.hektor.core.Dispatcher;

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
     * @param actor
     * @return
     */
    void register(ActorBox actor);

    void unregister(ActorBox actor);
}
