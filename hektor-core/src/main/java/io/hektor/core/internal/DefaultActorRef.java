package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Dispatcher;
import io.hektor.core.internal.messages.Watch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
                final DefaultActorContext bufCtx = (DefaultActorContext) ctx;
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
    public CompletionStage<Object> ask(final Object msg, final ActorRef asker) {
        final ActorContext ctx = Actor._ctx.get();
        try {
            if (ctx != null) {
                return ((DefaultActorContext) ctx).ask(msg, this);
            }
        } catch (final ClassCastException e) {
            // odd, should have been a DefaultActorContext...
        }

        return dispatcher.ask(asker, this, msg);
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
    public void monitor(final ActorRef ref) {
        dispatcher.dispatch(this, ref, Watch.MSG);
    }

    @Override
    public String toString()  {
        return path.toString();
    }
}
