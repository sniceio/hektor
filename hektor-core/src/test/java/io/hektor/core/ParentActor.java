package io.hektor.core;

import java.util.concurrent.CountDownLatch;

/**
 * @author jonas@jonasborjesson.com
 */
public class ParentActor implements Actor {

    @Override
    public void onReceive(final ActorContext context, final Object msg) {
        try {
            final DummyMessage message = (DummyMessage)msg;
            final Props child = Props.forActor(ChildActor.class).withConstructorArg(message.latch).build();
            final ActorRef childRef = ctx().actorOf(message.nameOfChild, child);
            childRef.tell(msg, context.sender());
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Strange", e);
        }
    }

    public static class DummyMessage {
        public final String nameOfChild;
        public final CountDownLatch latch;

        public DummyMessage(final String nameOfChild, final CountDownLatch latch) {
            this.nameOfChild = nameOfChild;
            this.latch = latch;
        }

    }
}
