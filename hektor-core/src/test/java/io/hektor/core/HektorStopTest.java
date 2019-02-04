package io.hektor.core;

import org.junit.Ignore;
import org.junit.Test;
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
 * A set of tests that is focusing on stopping actors and stopping the entire
 * system.
 *
 */
public class HektorStopTest extends HektorTestBase {
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
     * Ensure we can shut down the entire system.
     * @throws Exception
     */
    @Ignore
    @Test(timeout = 500)
    public void testStopHektor() throws Exception {
        final LatchContext latch001 = new LatchContext();
        final ActorRef parent01 =  defaultHektor.actorOf(latch001.parentProps(), "parent01");

        final LatchContext latch002 = new LatchContext();
        final ActorRef parent02 =  defaultHektor.actorOf(latch002.parentProps(), "parent02");

        defaultHektor.terminate();

        latch001.awaitShutdownLatches();
        latch002.awaitShutdownLatches();

    }
}
