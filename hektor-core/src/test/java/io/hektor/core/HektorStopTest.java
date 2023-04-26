package io.hektor.core;

import io.hektor.core.ParentActor.StopYourselfMessage;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.hektor.core.StoppingActor.StopMessage.postStop;
import static io.hektor.core.StoppingActor.StopMessage.stop;
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
        ref.tellAnonymously(new StopYourselfMessage());

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
    @Test(timeout = 2000)
    public void testStopHektor() throws Exception {
        final LatchContext latch001 = new LatchContext();
        defaultHektor.actorOf(latch001.parentProps(), "parent01");

        final LatchContext latch002 = new LatchContext();
        defaultHektor.actorOf(latch002.parentProps(), "parent02");

        final var future = defaultHektor.terminate();
        future.toCompletableFuture().get();
    }

    /**
     * An actor can still send out messages in its stop method.
     */
    @Test
    public void testSendMsgAtStop() throws Exception {
        ensureStopMessages(true, "hello");
        ensureStopMessages(true, "hello", "world");

        ensureStopMessages(false, "should", "be", "poststop");
    }

    private void ensureStopMessages(final boolean atStop, final String... msgs) throws Exception {
        final List<Object> cache = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(msgs.length);
        final ActorRef r = defaultHektor.actorOf(CachingActor.props(cache, latch), "caching");
        final ActorRef s = defaultHektor.actorOf(StoppingActor.props(), "stop");

        Arrays.stream(msgs).forEach(msg -> {
            s.tell(atStop ? stop(r, msg) : postStop(r, msg));
        });

        s.tell(new StopYourselfMessage());

        latch.await(500, TimeUnit.MILLISECONDS);

        synchronized (cache) {
            assertThat(cache, is(Arrays.asList(msgs)));
        }
    }
}
