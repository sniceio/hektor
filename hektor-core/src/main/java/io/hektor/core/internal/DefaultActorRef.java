package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorContext;
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
        final ActorContext ctx = Actor._ctx.get();
        if (ctx != null) {
            try {
                final DefaultActorContext bufCtx = (DefaultActorContext)ctx;
                bufCtx.buffer(msg, this, sender);
            } catch (final ClassCastException e) {
                // ignore. shouldn't happen but if it does
                // then just dispatch the message.
                dispatcher.dispatch(sender, this, msg);
            }
        } else {
            dispatcher.dispatch(sender, this, msg);
        }
    }

    @Override
    public void tell(Priority priority, Object msg, ActorRef sender) {
        tell(msg, sender);
    }

    @Override
    public void tellAnonymously(final Object msg) {
        dispatcher.dispatch(ActorRef.None(), this, msg);
    }

    @Override
    public String toString()  {
        return path.toString();
    }
}
