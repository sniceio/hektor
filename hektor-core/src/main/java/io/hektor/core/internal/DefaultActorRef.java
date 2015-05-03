package io.hektor.core.internal;

import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Dispatcher;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultActorRef implements ActorRef {

    /**
     * The path to the actor.
     */
    private final ActorPath path;

    private final Dispatcher dispatcher;

    public DefaultActorRef(final ActorPath path, final Dispatcher dispatcher) {
        this.path = path;
        this.dispatcher = dispatcher;
    }

    @Override
    public ActorPath path() {
        return path;
    }

    @Override
    public void tell(final Object msg, final ActorRef sender) {
        dispatcher.dispatch(sender, this, msg);
    }

    @Override
    public void tellAnonymously(final Object msg) {
        dispatcher.dispatch(null, this, msg);
    }
}
