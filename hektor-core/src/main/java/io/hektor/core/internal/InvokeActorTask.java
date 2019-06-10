package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.LifecycleEvent;
import io.hektor.core.internal.messages.Start;
import io.hektor.core.internal.messages.Stop;
import io.hektor.core.internal.messages.Watch;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
    private final InternalHektor hektor;
    private final CompletableFuture<Object> askFuture;

    public static InvokeActorTask create(final InternalHektor hektor, final ActorRef sender, final ActorRef receiver, final Object msg, final CompletableFuture<Object> askFuture) {
        assertNotNull(hektor);
        assertNotNull(sender);
        assertNotNull(receiver);
        assertNotNull(msg);
        return new InvokeActorTask(hektor, sender, receiver, msg, askFuture);
    }

    private InvokeActorTask(final InternalHektor hektor, final ActorRef sender, final ActorRef receiver, final Object msg, final CompletableFuture<Object> askFuture) {
        this.hektor = hektor;
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.askFuture = askFuture;
    }

    private Optional<DefaultActorContext> invokeActor(final ActorBox box, final Object msg) {
        final DefaultActorContext ctx = new DefaultActorContext(hektor, box, sender);
        try {
            Actor._ctx.set(ctx);
            if (msg == Start.MSG) {
                box.actor().start();
            } else {
                box.actor().onReceive(msg);
            }

            return Optional.of(ctx);
        } catch (final Throwable t) {
            t.printStackTrace();
        } finally {
            Actor._ctx.remove();
        }

        return Optional.empty();
    }

    /**
     * For whatever reason the actor has been asked to stop so we will initiate the stopping
     * sequence by first letting the user know that the actor is being stopped and then
     * start stopping all the children, if any, of this actor.
     *
     * @param box
     */
    private Optional<DefaultActorContext> initiateStoppingOfActor(final ActorBox box) {
        final DefaultActorContext ctx = new DefaultActorContext(hektor, box, sender);
        try {
            Actor._ctx.set(ctx);
            box.actor().stop();

            if (box.hasNoChildren()) {
                purgeActor(box);
            } else {
                box.stopChildren();
            }

            return Optional.of(ctx);
        } catch (final Throwable t) {
            t.printStackTrace();
        } finally {
            Actor._ctx.remove();
        }

        return Optional.empty();
    }

    /**
     * Once a child has been fully stopped we will remove it from the context
     * of this parent actor. If there are no more children of this actor
     * then we will also be purged.
     *
     * @param box
     * @param child
     */
    private void processStoppedChild(final ActorBox box, final Terminated child) {
        box.removeChild(child.actor().name());
    }

    /**
     * Once all the children of this actor has been called and there is nothing
     * else to do then finally completely remove this actor from the system.
     *
     * @param box
     */
    private void purgeActor(final ActorBox box) {
        hektor.removeActor(receiver);
        box.actor().postStop();
        final ActorPath me = receiver.path();
        me.parent().ifPresent(parentPath -> {
            final Terminated terminated = Terminated.of(me);
            hektor.lookup(parentPath).ifPresent(parent -> parent.tell(terminated, receiver));
        });

        box.tellWatchers(LifecycleEvent.terminated(me));
    }

    @Override
    public void run() {
        final Optional<ActorBox> actorBox = hektor.lookupActorBox(receiver);
        if (!actorBox.isPresent()) {
            return;
        }

        final ActorBox box = actorBox.get();
        final boolean isStopping = box.isStopped();

        if (msg == Stop.MSG) {
            box.stop();
        } else if (msg == Watch.MSG) {
            box.watch(sender);
        } else {
            final Object actualMsg;
            if (msg instanceof Terminated) {
                // the Terminated event is an internal event. We'll convert it
                // to the public {@link LifecycleEvent} before handing it off
                // to the actor. The reason is we want ot rely on the fact that
                // no one else can send us the Terminated event.
                final Terminated term = (Terminated)msg;
                actualMsg = LifecycleEvent.terminated(term.actor());
                processStoppedChild(box, term);
            } else {
                actualMsg = msg;
            }

            final Optional<DefaultActorContext> ctx = invokeActor(box, actualMsg);

            if (askFuture != null && !ctx.isPresent()) {
                askFuture.complete(null);
            }

            processActorContext(box, ctx);
        }

        // only call stop on the actor once
        if (isStopping ^ box.isStopped()) {
            final Optional<DefaultActorContext> ctx = initiateStoppingOfActor(box);
            processActorContext(box, ctx);
        } else if ((isStopping || box.isStopped()) && box.hasNoChildren()) {
            purgeActor(box);
        }
    }

    private void processActorContext(final ActorBox box, final Optional<DefaultActorContext> ctx) {
        ctx.ifPresent(c -> {
            c.bufferedMessages().stream().forEach(e -> {
                if (askFuture != null && e.receiver().equals(sender)) {
                    askFuture.complete(e.message());
                } else {
                    e.receiver().tell(Priority.HIGH, e.message(), e.sender());
                }
            });

            if (c.isStopped()) {
                box.stop();
            }
        });
    }
}
