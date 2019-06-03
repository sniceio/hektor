package io.hektor.core;

import io.hektor.core.internal.Terminated;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @author jonas@jonasborjesson.com
 */
public class ParentActor implements Actor {

    private final CountDownLatch latch;
    private final CountDownLatch stopLatch;
    private final CountDownLatch postStopLatch;
    private final CountDownLatch terminatedLatch;

    /**
     * The Cancellable if there are any outstanding delayed tasks.
     */
    private Optional<Cancellable> cancellable;

    public ParentActor() {
        this(new CountDownLatch(1), new CountDownLatch(1), new CountDownLatch(1));
    }

    public ParentActor(final CountDownLatch latch) {
        this(latch, new CountDownLatch(1), new CountDownLatch(1));
    }

    public ParentActor(final CountDownLatch latch, final CountDownLatch stopLatch) {
        this(latch, stopLatch, new CountDownLatch(1));
    }

    public ParentActor(final CountDownLatch latch, final CountDownLatch stopLatch, final CountDownLatch postStopLatch) {
        this(latch, stopLatch, postStopLatch, new CountDownLatch(1));
    }

    public ParentActor(final CountDownLatch latch, final CountDownLatch stopLatch,
                       final CountDownLatch postStopLatch, final CountDownLatch terminatedLatch) {
        this.latch = latch;
        this.stopLatch = stopLatch;
        this.postStopLatch = postStopLatch;
        this.terminatedLatch = terminatedLatch;
    }

    @Override
    public void stop() {
        stopLatch.countDown();
    }

    public void postStop() {
        postStopLatch.countDown();
    }

    @Override
    public void onReceive(final Object msg) {
        if (msg instanceof DummyMessage) {
            final DummyMessage message = (DummyMessage) msg;
            final Props child = Props.forActor(ChildActor.class, () -> new ChildActor(message.latch));
            final ActorRef childRef = ctx().actorOf(message.nameOfChild, child);
            childRef.tell(msg, sender());
        } else if (msg instanceof CancelScheduledTask) {
            cancellable.ifPresent(c -> c.cancel());
        } else if (msg instanceof TimedMessage) {
            final TimedMessage timed = (TimedMessage) msg;
            cancellable = Optional.of(ctx().scheduler().schedule(timed.msg, timed.sender, timed.receiver, timed.delay));
        } else if (msg instanceof Terminated) {
            terminatedLatch.countDown();
        } else if (msg instanceof CreateChildMessage) {
            final CreateChildMessage message = (CreateChildMessage) msg;
            final Props child = Props.forActor(ChildActor.class, () -> new ChildActor(message.latch));
            ctx().actorOf(message.nameOfChild, child);
        } else if (msg instanceof StopYourselfMessage) {
            ctx().stop();
        }
        latch.countDown();
    }

    /**
     * Send this message to have the actor stop itself.
     */
    public static class StopYourselfMessage {
    }

    /**
     * Send this message to ask the actor to cancel any tasks
     * it has outstanding.
     */
    public static class CancelScheduledTask {
    }

    /**
     * Send this message to an actor and ask it to schedule a timer
     * that when fired sends the other message
     */
    public static class TimedMessage {
        public final Duration delay;
        public final Object msg;
        public final ActorRef receiver;
        public final ActorRef sender;

        public TimedMessage(final Object msg, ActorRef receiver, ActorRef sender, final Duration delay) {
            this.delay = delay;
            this.msg = msg;
            this.receiver = receiver;
            this.sender = sender;
        }
    }

    public static class TalkToSiblingMessage {
        public final String msg;
        public final String sibling;

        public TalkToSiblingMessage(final String sibling, final String msg) {
            this.sibling = sibling;
            this.msg = msg;
        }
    }

    public static class CreateChildMessage {
        public final String nameOfChild;
        public final CountDownLatch latch;

        public CreateChildMessage(final String child) {
            this(child, new CountDownLatch(1));
        }

        public CreateChildMessage(final String child, final CountDownLatch latch) {
            this.nameOfChild = child;
            this.latch = latch;
        }
    }

    public static class DummyMessage {
        public final String nameOfChild;
        public final CountDownLatch latch;

        /**
         * Our sibling and if configured then we will send a hello msg to it
         */
        public final String sibling;

        public DummyMessage(final String nameOfChild, final CountDownLatch latch) {
            this(nameOfChild, latch, null);
        }

        public DummyMessage(final String nameOfChild, final CountDownLatch latch, final String sibling) {
            this.nameOfChild = nameOfChild;
            this.latch = latch;
            this.sibling = sibling;
        }

    }
}
