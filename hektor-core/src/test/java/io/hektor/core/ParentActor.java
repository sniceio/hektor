package io.hektor.core;

import java.util.concurrent.CountDownLatch;

/**
 * @author jonas@jonasborjesson.com
 */
public class ParentActor implements Actor {

    private final CountDownLatch latch;
    private final CountDownLatch stopLatch;
    private final CountDownLatch postStopLatch;

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
        this.latch = latch;
        this.stopLatch = stopLatch;
        this.postStopLatch = postStopLatch;
    }

    @Override
    public void stop() {
        stopLatch.countDown();;
    }

    public void postStop() {
        postStopLatch.countDown();;
    }

    @Override
    public void onReceive(final ActorContext context, final Object msg) {
        try {
            if (msg instanceof DummyMessage) {
                final DummyMessage message = (DummyMessage) msg;
                final Props child = Props.forActor(ChildActor.class).withConstructorArg(message.latch).build();
                final ActorRef childRef = ctx().actorOf(message.nameOfChild, child);
                childRef.tell(msg, context.sender());
            } else if (msg instanceof Terminated) {
                System.err.println("A child of mine was just terminated: " + ((Terminated)msg).actor());
            } else if (msg instanceof CreateChildMessage) {
                final CreateChildMessage message = (CreateChildMessage)msg;
                final Props child = Props.forActor(ChildActor.class).withConstructorArg(message.latch).build();
                ctx().actorOf(message.nameOfChild, child);
            } else if (msg instanceof StopYourselfMessage) {
                ctx().stop();
            }
            latch.countDown();
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Strange", e);
        }
    }

    /**
     * Send this message to have the actor stop itself.
     */
    public static class StopYourselfMessage {
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
