package io.hektor.core;

import org.junit.Test;

import java.util.List;
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

    @Test(timeout = 500)
    public void testActorWithRouter() throws Exception {
        final int count = 4;
        final CountDownLatch latches[] = new CountDownLatch[count];
        final Router router = null;

        Hektor.RouterBuilder routerBuilder = defaultHektor.routerWithName("hello-router");
        routerBuilder.withRoutingLogic(new SimpleRoutingLogic());

        for (int i = 0; i < count; ++i) {
            latches[i] = new CountDownLatch(1);
            final Props props = Props.forActor(DummyActor.class).withConstructorArg(latches[i]).build();
            final ActorRef ref = defaultHektor.actorOf(props, "hello-" + i);
            routerBuilder.withRoutee(ref);
        }

        final ActorRef routerRef = routerBuilder.build();
        routerRef.tellAnonymously(1);
        latches[1].await();

        routerRef.tellAnonymously(0);
        latches[0].await();

        routerRef.tellAnonymously(3);
        latches[3].await();

        routerRef.tellAnonymously(2);
        latches[2].await();
    }

    /**
     * Super simple routing logic that assumes that the message is an Integer and
     * uses it as an index into the list of routees when selcting which actor
     * should get the message.
     */
    private static class SimpleRoutingLogic implements RoutingLogic {

        @Override
        public ActorRef select(Object msg, List<ActorRef> routees) {
            Integer index = (Integer)msg;
            return routees.get(index);
        }
    }
}