package io.hektor.fsm;

import io.hektor.fsm.builder.FSMBuilder;
import io.hektor.fsm.builder.StateBuilder;
import io.hektor.fsm.builder.exceptions.StateBuilderException;
import io.hektor.fsm.builder.exceptions.TransientLoopDetectedException;
import io.hektor.fsm.builder.exceptions.TransientStateMissingTransitionException;
import io.hektor.fsm.builder.exceptions.TransitionMissingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test our transient states.
 *
 * A transient state is one that you enter and exit in the same "firing".
 */
public class TransientStateTest {

    private static final Logger logger = LoggerFactory.getLogger(TransientStateTest.class);

    private FSM<SuperSimpleStates, Context, Data> fsm;
    private FSMBuilder<SuperSimpleStates, Context, Data> builder;
    private StateBuilder<SuperSimpleStates, Context, Data> a;
    private StateBuilder<SuperSimpleStates, Context, Data> b;
    private StateBuilder<SuperSimpleStates, Context, Data> c;
    private StateBuilder<SuperSimpleStates, Context, Data> d;
    private StateBuilder<SuperSimpleStates, Context, Data> e;
    private StateBuilder<SuperSimpleStates, Context, Data> f;
    private StateBuilder<SuperSimpleStates, Context, Data> g;
    private StateBuilder<SuperSimpleStates, Context, Data> h;

    @Before
    public void setUp() {
        builder = FSM.of(SuperSimpleStates.class).ofContextType(Context.class).withDataType(Data.class);

        // for simplicity sake, all our unit tests will have the intial state set as A and
        // the final state as H.
        a = builder.withInitialState(SuperSimpleStates.A);
        h = builder.withFinalState(SuperSimpleStates.H);
    }

    /**
     * Ensure that you can't get away with not specifying any state
     * for a transient state.
     *
     * @throws Exception
     */
    @Test
    public void testTransientStateNoTransitionAtAll() throws Exception {
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class);
        builder.withTransientState(SuperSimpleStates.B);
        ensureBuildFsmFails(TransitionMissingException.class);
    }

    /**
     * Ensure that you can't get away with a transient state that doesn't have a default
     * transition.
     *
     * @throws Exception
     */
    @Test
    public void testTransientStateNoDefaultTransition() throws Exception {
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class);
        b = builder.withTransientState(SuperSimpleStates.B);
        b.transitionTo(SuperSimpleStates.H).onEvent(String.class);
        ensureBuildFsmFails(TransientStateMissingTransitionException.class);
    }

    /**
     * Ensure that you cannot define a transition back to yourself (you being the
     * transient state). If you could, we would create a loop...
     *
     * @throws Exception
     */
    @Test(expected = TransientLoopDetectedException.class)
    public void testTransientStateLoop() throws Exception {
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class);
        b = builder.withTransientState(SuperSimpleStates.B);

        // should fail
        b.transitionTo(SuperSimpleStates.B).asDefaultTransition();
    }

    /**
     * This one is a little trickier because we transitions
     * from B to C, which are both transient states, which
     * currently isn't allowed.
     *
     * NOTE: ignoring for now because B to C should be fine unless they come back
     * in a loop
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testTransientStateLoopBtoC() throws Exception {
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class);
        b = builder.withTransientState(SuperSimpleStates.B);
        c = builder.withTransientState(SuperSimpleStates.C);

        // Note that this doesn't fail at this point in time because
        // the state C may not even have been defined yet, which is why
        // we're just using enums here. Which of course means we cannot
        // check certain things at this stage.
        b.transitionTo(SuperSimpleStates.C).asDefaultTransition();

        c.transitionTo(SuperSimpleStates.B).asDefaultTransition();

        // but when we build the entier FSM we'll be able to figure it out.
        ensureBuildFsmFails(TransientLoopDetectedException.class);
    }

    private void ensureBuildFsmFails(final Class<? extends Throwable> expectedException) throws Exception {
        try {
            build();
            fail("Expected the building of the FSM would fail");
        } catch (final StateBuilderException e) {
            final String msg = "Unable to cast " + e.getClass().getName() + " into the expected exception of "
                    + expectedException.getName();
            assertThat(msg, expectedException.isInstance(e), is(true));
        }
    }

    /**
     * In this super simple test, A is initial, B is a transient state and H is the final
     * state. This means that as soon as a transition occurs from A to B, we will immediately
     * transition to H and the FSM would be done.
     */
    @Test
    public void testBasicTransientState() throws Exception {
        final CountDownLatch onEnterB = new CountDownLatch(1);
        final CountDownLatch onExitB = new CountDownLatch(1);
        final CountDownLatch bTransition = new CountDownLatch(1);
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class);

        b = builder.withTransientState(SuperSimpleStates.B);
        b.withEnterAction((ctx, data) -> onEnterB.countDown());
        b.withExitAction((ctx, data) -> onExitB.countDown());
        b.transitionTo(SuperSimpleStates.H).asDefaultTransition().withAction(o -> bTransition.countDown());

        go("hello");

        ensureLatches(onEnterB, onExitB, bTransition);
        assertThat(fsm.isTerminated(), is(true));
    }

    /**
     * Specify different paths through the transient state and ensure that
     * he one we want to have executed is indeed executed.
     * @throws Exception
     */
    @Test
    public void testWithMultipleTransitions() throws Exception {
        final CountDownLatch no = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class);

        b = builder.withTransientState(SuperSimpleStates.B);

        // this is the event we want to be used.
        b.transitionTo(SuperSimpleStates.H).onEvent(String.class).withAction(o -> latch.countDown());

        // and the default one should not be used.
        b.transitionTo(SuperSimpleStates.H).asDefaultTransition().withAction(o -> no.countDown());

        go("hello");
        ensureLatches(latch);
        ensureLatchesNotOpened(no);
        assertThat(fsm.isTerminated(), is(true));
    }

    private void go(final Object event) {
        fsm = build();
        fsm.start();
        fsm.onEvent(event);
    }

    private FSM<SuperSimpleStates, Context, Data> build() {
        return builder.build().newInstance("uuid-123", Mockito.mock(Context.class), Mockito.mock(Data.class),
                TransientStateTest::onUnhandledEvent, TransientStateTest::onTransition);
    }

    private void ensureLatchesNotOpened(final CountDownLatch... latches) throws Exception {
        for (final CountDownLatch latch : latches) {
            if (latch.getCount() == 0) {
                fail("The latch is opened but it shouldn't be");
            }
        }
    }

    private void ensureLatches(final CountDownLatch... latches) throws Exception {
        for (final CountDownLatch latch : latches) {
            if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
                fail("Waited for one of the latches to open but it didn't");
            }
        }
    }

    private static void onUnhandledEvent(final SuperSimpleStates state, final Object event) {
        fail("I did not expect a unhandled event");
    }

    private static void onTransition(final SuperSimpleStates from, final SuperSimpleStates to, final Object event) {
        logger.info("{} -> {} Event: {}", from, to, event);
    }

    private enum SuperSimpleStates {
        A, B, C, D, E, F, G, H;
    }
}
