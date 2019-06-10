package io.hektor.core;

import io.hektor.core.ParentActor.DummyMessage;
import io.hektor.core.ParentActor.TalkToSiblingMessage;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @author jonas@jonasborjesson.com
 */
public class ChildActor implements Actor {

    private final CountDownLatch latch;
    private final CountDownLatch latch2;
    private final CountDownLatch latch3;

    public ChildActor(final CountDownLatch latch) {
        this(latch, null, null);
    }

    public ChildActor(final CountDownLatch latch, final CountDownLatch latch2) {
        this(latch, latch2, null);
    }

    public ChildActor(final CountDownLatch latch, final CountDownLatch latch2, final CountDownLatch latch3) {
        this.latch = latch;
        this.latch2 = latch2;
        this.latch3 = latch3;
    }

    @Override
    public void onReceive(final Object msg) {
        // if we have a ref in the dummy msg then send a message
        // over to that actor as well.
        if (msg instanceof DummyMessage) {
            if (latch.getCount() > 0) {
                latch.countDown();
            } else if (latch2 != null) {
                latch2.countDown();
            }

            final DummyMessage dummyMsg = (DummyMessage) msg;

            // if we have a sibling, then say hello... this is just to test
            // so that it is possible to lookup a sibling from the parent ref
            // and issue a message to it...
            if (dummyMsg.sibling != null) {
                final Optional<ActorRef> sibling = ctx().lookup("../" + dummyMsg.sibling);
                if (sibling.isPresent()) {
                    sibling.get().tell("hello sibling!", self());
                }
            }
        } else if (msg instanceof TalkToSiblingMessage){
            // i guess we are supposed to talk to one of our siblings, let's do that!
            final TalkToSiblingMessage talkMsg = (TalkToSiblingMessage)msg;
            final Optional<ActorRef> sibling = ctx().lookup("../" + talkMsg.sibling);
            sibling.ifPresent(ref -> ref.tell(talkMsg.msg, self()));
        } else if (msg instanceof ParentActor.StopYourselfMessage){
            ctx().stop();
        } else if (msg instanceof String){
            if (msg.toString().contains("hello") && latch.getCount() > 0) {
                latch.countDown();
                sender().tell("hello back", self());
            }
            System.out.println("Received another msg: " + msg);
        } else {
            System.err.println("Ok so I'm a child and got this: " + msg);
        }
    }
}
