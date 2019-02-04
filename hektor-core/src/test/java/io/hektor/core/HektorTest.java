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
        final ActorRef ref = defaultHektor.actorOf(DummyActor.props(defaultLatch1), "hello");
        ref.tellAnonymously("hello world");
        defaultLatch1.await();
    }

    @Test(timeout = 500)
    public void testSimpleReplyToMessage() throws Exception {
        final ActorRef ref1 = defaultHektor.actorOf(DummyActor.props(defaultLatch1, false), "first");
        final ActorRef ref2 = defaultHektor.actorOf(DummyActor.props(defaultLatch2, true), "second");

        ref2.tell("hello no 2", ref1);
        defaultLatch1.await();
        defaultLatch2.await();
    }

    @Test(timeout = 500)
    public void testSendToSelf() throws Exception {
        final CountDownLatch latch = new CountDownLatch(10);
        final ActorRef selfish = defaultHektor.actorOf(SelfishActor.props(latch), "selfish");
        selfish.tellAnonymously("You're the best");
        latch.await();
    }

    @Test(timeout = 500)
    public void testActorWithRouter() throws Exception {
        final int count = 4;
        final CountDownLatch latches[] = new CountDownLatch[count];

        Hektor.RouterBuilder routerBuilder = defaultHektor.routerWithName("hello-router");
        routerBuilder.withRoutingLogic(new SimpleRoutingLogic());

        for (int i = 0; i < count; ++i) {
            latches[i] = new CountDownLatch(1);
            final ActorRef ref = defaultHektor.actorOf(DummyActor.props(latches[i]), "hello-" + i);
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
        final Props props = Props.forActor(ParentActor.class, () -> new ParentActor());
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


}