package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;

import java.util.List;

/**
 * Every message that is being passed to an actor is by default going through
 * this Runnable. It's job is to lookup the actor, invoke the onReceive method,
 * deal with any new messages that are being sent from the actor as a result of
 * this invocation and also potentially deal
 * with any exceptions that is being thrown.
 *
 * Hence, this is an important little class.
 *
 * @author jonas@jonasborjesson.com
 */
public class InvokeActorTask implements Runnable {

    private final ActorStore actorStore;
    private final ActorRef sender;
    private final ActorRef receiver;
    private final Object msg;

    public InvokeActorTask(final ActorStore actorStore, final ActorRef sender, final ActorRef receiver, final Object msg) {
        this.actorStore = actorStore;
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            final BufferingActorContext ctx = new BufferingActorContext(receiver, sender);
            final ActorBox actorBox = actorStore.lookup(receiver);
            final Actor actor = actorBox.actor();
            actor.onReceive(ctx, msg);

            final List<Envelope> messages = ctx.messages();
            for (final Envelope envelope : messages) {
                envelope.receiver().tell(envelope.message(), envelope.sender());
            }
        } catch (final Throwable t) {
            // Note: if an Actor throws an exception we will not
            // dispatch any of the messages it tried to send during
            // this invocation. This is done on purpose and according
            // to contract as documented on the Actor interface.
            t.printStackTrace();
        }
    }
}
