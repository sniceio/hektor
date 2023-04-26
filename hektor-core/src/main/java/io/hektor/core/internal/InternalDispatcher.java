package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;
import io.hektor.core.Dispatcher;

import java.util.concurrent.CompletionStage;

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

    CompletionStage<Void> shutdown();
}
