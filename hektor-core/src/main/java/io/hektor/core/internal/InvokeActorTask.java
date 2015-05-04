package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;

import java.util.List;

import static io.hektor.core.internal.PreConditions.assertNotNull;

/**
 * Every message that is being passed to an actor is by default going through
 * this Runnable. It's job is to lookup the actor, invoke the onReceive method,
 * deal with any new messages that are being sent from the actor as a result of
 * this invocation and also potentially deal with any exceptions that is being thrown.
 *
 * @author jonas@jonasborjesson.com
 */
public class InvokeActorTask implements Runnable {

    private final ActorRef sender;
    private final ActorRef receiver;
    private final Object msg;
    private final InternalDispatcher dispatcher;

    public static InvokeActorTask create(final InternalDispatcher dispatcher, final ActorRef sender, final ActorRef receiver, final Object msg) {
        assertNotNull(dispatcher);
        assertNotNull(sender);
        assertNotNull(receiver);
        assertNotNull(msg);
        return new InvokeActorTask(dispatcher, sender, receiver, msg);
    }

    private InvokeActorTask(final InternalDispatcher dispatcher, final ActorRef sender, final ActorRef receiver, final Object msg) {
        this.dispatcher = dispatcher;
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
    }

    @Override
    public void run() {
        Actor actor = null;
        try {
            final ActorBox actorBox = dispatcher.lookup(receiver);
            System.err.println(actorBox.ref().path());
            final BufferingActorContext ctx = new BufferingActorContext(dispatcher, actorBox, sender);
            actor = actorBox.actor();
            System.err.println(Thread.currentThread().getName() + " Setting the actor context " + actor._ctx);
            actor._ctx.set(ctx);
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
        } finally {
            if (actor != null) {
                System.err.println("Removing the actor context again " + actor._ctx);
                actor._ctx.remove();
            }
        }
    }
}
