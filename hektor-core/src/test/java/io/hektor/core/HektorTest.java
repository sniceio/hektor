package io.hektor.core;

import io.hektor.core.ParentActor.CancelScheduledTask;
import io.hektor.core.ParentActor.CreateChildMessage;
import io.hektor.core.ParentActor.DummyMessage;
import io.hektor.core.ParentActor.TimedMessage;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        final Props props = Props.forActor(DummyActor.class).withConstructorArg(defaultLatch1).build();
        final ActorRef ref = defaultHektor.actorOf(props, "hello");
        ref.tellAnonymously("hello world");
        defaultLatch1.await();
    }

    @Test(timeout = 500)
    public void testSimpleReplyToMessage() throws Exception {
        final Props props1 = Props.forActor(DummyActor.class).withConstructorArg(defaultLatch1).build();
        final ActorRef ref1 = defaultHektor.actorOf(props1, "first");

        final Props props2 = Props.forActor(DummyActor.class).withConstructorArg(defaultLatch2).withConstructorArg(true).build();
        final ActorRef ref2 = defaultHektor.actorOf(props2, "second");

        ref2.tell("hello no 2", ref1);
        defaultLatch1.await();
        defaultLatch2.await();
    }

    @Test(timeout = 500)
    public void testActorWithRouter() throws Exception {
        final int count = 4;
        final CountDownLatch latches[] = new CountDownLatch[count];

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
        routerRef.tellAnonymously(1);
        routerRef.tellAnonymously(1);
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
     * Every actor can create actors on its own to which it becomes the parent and
     * supervisor.
     *
     * @throws Exception
     */
    @Test(timeout = 500)
    public void testActorCreatingChildren() throws Exception {
        final Props props = Props.forActor(ParentActor.class).build();
        final ActorRef ref = defaultHektor.actorOf(props, "parent");
        final DummyMessage msgAlice = new DummyMessage("alice", defaultLatch1);
        final DummyMessage msgBob = new DummyMessage("bob", defaultLatch2, "alice");
        ref.tellAnonymously(msgAlice);
        ref.tellAnonymously(msgBob);
        defaultLatch1.await();;
        defaultLatch2.await();
    }

    /**
     * Create two siblings and have them send a message between each
     * other using the context.lookup function.
     * @throws Exception
     */
    @Test(timeout = 500)
    public void testSiblingSendMessageToOtherSibling() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        final ActorRef ref = createParentActor(latch);
        ref.tellAnonymously(new CreateChildMessage("romeo", defaultLatch1));
        ref.tellAnonymously(new CreateChildMessage("julia", defaultLatch2));

        // wait to ensure that the parent actor got
        // both messages to create new children
        latch.await();

        final Optional<ActorRef> julia = defaultHektor.lookup("./parent/julia");
        final Optional<ActorRef> romeo = defaultHektor.lookup("./parent/romeo");

        assertThat(julia.isPresent(), is(true));
        assertThat(romeo.isPresent(), is(true));

        // send message to julia from romeo.
        // it is expected that julia says hello back
        // hence the hanging on the two latches...
        julia.get().tell("hello julia", romeo.get());
        defaultLatch1.await();
        defaultLatch2.await();
    }

    /**
     * It must of course be possible to stop an actor.
     *
     * @throws Exception
     */
    @Test(timeout = 500)
    public void testStoppingAnActor() throws Exception {
        final ActorRef ref = createParentActor();

        // as soon as we have a ref the actor must be available for
        // lookup in the system
        assertThat(defaultHektor.lookup("parent").isPresent(), is(true));
        assertThat(defaultHektor.lookup(ref.path()).isPresent(), is(true));

        // ask the actor to stop itself.
        ref.tellAnonymously(new ParentActor.StopYourselfMessage());

        // make sure all the various methods are called, which
        // are controlled by the different latches. Note, we have
        // to call all latches because otherwise we wouldn't
        // know if e.g. postStop was called but not stop()
        defaultLatch1.await();
        defaultStopLatch1.await();
        defaultPostStopLatch1.await();

        // after post stop the actor MUST be completely gone from
        // the system.
        assertThat(defaultHektor.lookup("parent").isPresent(), is(false));
        assertThat(defaultHektor.lookup(ref.path()).isPresent(), is(false));
    }


    /**
     * Make sure that we can actually schedule a task by asking one actor to send
     * a delayed task to another where the message is to kill yourself. Hence, if
     * the actor asked to stop itself gets stopped we should be able to pick that
     * up from the stop latch.
     *
     * @throws Exception
     */
    @Test(timeout = 1500)
    public void testScheduleTask() throws Exception {
        final ActorRef one = createParentActor();
        final ActorRef two = createParentActor();

        // this is the message we will send to one, who then will
        // send the StopYourselfMessage to two.
        final long ts = System.currentTimeMillis();
        final TimedMessage msg = new TimedMessage(new ParentActor.StopYourselfMessage(), two, one, Duration.ofMillis(1000));
        one.tellAnonymously(msg);
        defaultPostStopLatch1.await();
    }

    /**
     * Ensure that we actually can cancel a task as well.
     *
     * @throws Exception
     */
    @Test
    public void testScheduleTaskThenCancel() throws Exception {
        final ActorRef one = createParentActor();
        final ActorRef two = createParentActor();

        // this is the message we will send to one, who then will
        // send the StopYourselfMessage to two.
        final TimedMessage msg = new TimedMessage(new ParentActor.StopYourselfMessage(), two, one, Duration.ofMillis(1000));
        one.tellAnonymously(msg);
        one.tellAnonymously(new CancelScheduledTask());
        defaultPostStopLatch1.await(2000, TimeUnit.MILLISECONDS);

        // since we cancelled the task the post stop latch
        // should NOT have fired.
        assertThat(defaultPostStopLatch1.getCount(), is(1L));
    }

    @Test(timeout = 500)
    public void testStoppingAnActorWithChildren() throws Exception {
        final ActorRef ref = createDefaultParentTwoChildren();

        // tell the parent to stop itself.
        ref.tellAnonymously(new ParentActor.StopYourselfMessage());

        defaultStopLatch1.await();
        defaultPostStopLatch1.await();

        assertThat(defaultHektor.lookup("parent").isPresent(), is(false));
        assertThat(defaultHektor.lookup("./parent/julia").isPresent(), is(false));
        assertThat(defaultHektor.lookup("./parent/romeo").isPresent(), is(false));
    }

    /**
     * Create a parent, two children and ask one of the children to stop.
     *
     * @throws Exception
     */
    @Test(timeout = 500)
    public void testStoppingChildButNotParent() throws Exception {
        final ActorRef ref = createDefaultParentTwoChildren();

        defaultHektor.lookup("./parent/julia").get().tellAnonymously(new ParentActor.StopYourselfMessage());

        // we should have been getting a terminated event
        defaultTerminatedLatch1.await();

        // and therefore julia is no more...
        assertThat(defaultHektor.lookup("./parent/julia").isPresent(), is(false));

        // but the parent and romeo are both still around
        assertThat(defaultHektor.lookup("parent").isPresent(), is(true));
        assertThat(defaultHektor.lookup("./parent/romeo").isPresent(), is(true));
    }

    /**
     * Create a parent, two children and ask both children to stop.
     *
     * @throws Exception
     */
    @Test(timeout = 500)
    public void testStoppingBothChildren() throws Exception {
        final CountDownLatch terminatedLatch = new CountDownLatch(2);
        createDefaultParentTwoChildrenWithTerminatedLatch(terminatedLatch);

        defaultHektor.lookup("./parent/julia").get().tellAnonymously(new ParentActor.StopYourselfMessage());
        defaultHektor.lookup("./parent/romeo").get().tellAnonymously(new ParentActor.StopYourselfMessage());

        // we should have been getting a terminated event
        terminatedLatch.await();

        // and therefore julia is no more...
        assertThat(defaultHektor.lookup("./parent/julia").isPresent(), is(false));
        assertThat(defaultHektor.lookup("./parent/romeo").isPresent(), is(false));

        // but the parent and romeo are both still around
        assertThat(defaultHektor.lookup("parent").isPresent(), is(true));
    }

    private ActorRef createDefaultParentTwoChildren() throws Exception {
        return createDefaultParentTwoChildrenWithTerminatedLatch(defaultTerminatedLatch1);
    }

    /**
     * Convenience method for creating two children as well as specifying the terminated latch.
     *
     * @param terminatedLatch the terminated latch will be used by the parent as a countdown latch for any
     *                        terminated events it may receive.
     * @return
     * @throws Exception
     */
    private ActorRef createDefaultParentTwoChildrenWithTerminatedLatch(final CountDownLatch terminatedLatch) throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        final ActorRef ref = createParentActor(latch, defaultStopLatch1, defaultPostStopLatch1, terminatedLatch);
        ref.tellAnonymously(new CreateChildMessage("romeo"));
        ref.tellAnonymously(new CreateChildMessage("julia"));

        // wait to ensure that the parent actor got
        // both messages to create new children
        latch.await();

        // make sure that we now have to child actors
        assertThat(defaultHektor.lookup("./parent/julia").isPresent(), is(true));
        assertThat(defaultHektor.lookup("./parent/romeo").isPresent(), is(true));

        return ref;
    }

    /**
     * Super simple routing logic that assumes that the message is an Integer and
     * uses it as an index into the list of routees when selecting which actor
     * should get the message.
     */
    private static class SimpleRoutingLogic implements RoutingLogic {
        @Override
        public ActorRef select(Object msg, List<ActorRef> routees) {
            Integer index = (Integer)msg;
            return routees.get(index);
        }
    }

    /**
     * Convenience method for creating a parent actor under the name "parent" and that is
     * using the default latch no 1.
     *
     * @return
     * @throws Exception
     */
    private ActorRef createParentActor() throws Exception {
        return createParentActor(defaultLatch1, defaultStopLatch1, defaultPostStopLatch1, defaultTerminatedLatch1);
    }

    /**
     * Convenience method for creating a parent actor under the name "parent" and is using
     * the supplied latch.
     *
     * @param latch
     * @return
     * @throws Exception
     */
    private ActorRef createParentActor(final CountDownLatch latch) throws Exception {
        return createParentActor(latch, defaultStopLatch1, defaultPostStopLatch1, defaultTerminatedLatch1);
    }

    private ActorRef createParentActor(final CountDownLatch latch,
                                       final CountDownLatch stopLatch,
                                       final CountDownLatch postStopLatch) throws Exception {
        return createParentActor(latch, stopLatch, postStopLatch, defaultTerminatedLatch1);
    }

    private ActorRef createParentActor(final CountDownLatch latch,
                                       final CountDownLatch stopLatch,
                                       final CountDownLatch postStopLatch,
                                       final CountDownLatch terminatedLatch) throws Exception {
        final Props props = Props.forActor(ParentActor.class)
                .withConstructorArg(latch)
                .withConstructorArg(stopLatch)
                .withConstructorArg(postStopLatch)
                .withConstructorArg(terminatedLatch)
                .build();
        return defaultHektor.actorOf(props, "parent");
    }
}