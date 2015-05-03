package io.hektor.core;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Basic tests to configure and start Hektor and send some basic messages through.
 *
 * @author jonas@jonasborjesson.com
 */
public class HektorTest extends HektorTestBase {

    /**
     * Make sure that we can start the basic system and send
     * a single message through. We are using a countdown latch
     * and if that one doesn't complete within 500ms the test
     * will fail.
     *
     * @throws Exception
     */
    @Test(timeout = 500)
    public void testSimpleSendMessage() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Props props = Props.forActor(DummyActor.class).withConstructorArg(latch).build();
        final ActorRef ref = defaultHektor.actorOf(props, "hello");
        ref.tellAnonymously("hello world");
        latch.await();
    }

    @Test(timeout = 500)
    public void testSimpleReplyToMessage() throws Exception {
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        final Props props1 = Props.forActor(DummyActor.class).withConstructorArg(latch1).build();
        final ActorRef ref1 = defaultHektor.actorOf(props1, "first");

        final Props props2 = Props.forActor(DummyActor.class).withConstructorArg(latch2).withConstructorArg(true).build();
        final ActorRef ref2 = defaultHektor.actorOf(props2, "second");

        ref2.tell("hello no 2", ref1);
        latch1.await();
        latch2.await();
    }
}