package io.hektor.fsm;

import io.hektor.fsm.builder.StateBuilder;
import io.hektor.fsm.builder.exceptions.IllegalTransformationOnTransitionException;
import io.hektor.fsm.builder.exceptions.StateBuilderException;
import io.hektor.fsm.builder.exceptions.TransientLoopDetectedException;
import io.hektor.fsm.builder.exceptions.TransientStateMissingTransitionException;
import io.hektor.fsm.builder.exceptions.TransitionMissingException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test our transient states.
 *
 * A transient state is one that you enter and exit in the same "firing".
 */
public class TransientStateTest extends TestBase {

    private CountDownLatch latch;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        latch = new CountDownLatch(1);
    }

    /**
     * You can optionally specify an initial enter action, which is an action that will only ever
     * execute once and that is when you enter the state the very first time. Any subsequent "enters"
     * into that state will not cause this "initial enter action" to be executed.
     */
    @Test
    public void testInitialEnterAction() {
        final CountDownLatch initialEnterAction = mock(CountDownLatch.class);
        final CountDownLatch enterActionLatch = mock(CountDownLatch.class);

        b = builder.withState(SuperSimpleStates.B);
        c = builder.withState(SuperSimpleStates.C);

        b.withInitialEnterAction((ctx, data) -> initialEnterAction.countDown());
        b.withEnterAction((ctx, data) -> enterActionLatch.countDown());

        a.transitionTo(SuperSimpleStates.B).onEvent(String.class);
        b.transitionTo(SuperSimpleStates.H).onEvent(String.class).withGuard("exit"::equals);
        b.transitionTo(SuperSimpleStates.C).onEvent(String.class); // note: more generic so must be specified after the "exit" guard.

        // Note that we have to actually leave the state in order to enter it again.
        // Just going from B -> B doesnt trigger the enter/exit actions to be executed.
        c.transitionTo(SuperSimpleStates.B).onEvent(String.class);

        go("hello");
        verify(initialEnterAction).countDown();
        verify(enterActionLatch).countDown();

        // Transition B -> C -> B
        fsm.onEvent("going to C");
        fsm.onEvent("going from C -> B and should not have the initial enter action kicking in again");

        // still only once - and calling times(1) just to make it obvious what I want
        // (default is times(1) so it is not necessary but again, making it obvious)
        verify(initialEnterAction, times(1)).countDown();

        // the regular one should have been called twice though
        verify(enterActionLatch, times(2)).countDown();

        fsm.onEvent("exit");
        assertThat(fsm.isTerminated(), is(true));
    }

    /**
     * Ensure that the so-called "self" enter action is triggered only on self enter.
     */
    @Test
    public void testSelfEnterAction() {
        final CountDownLatch enterActionLatch = mock(CountDownLatch.class);
        final CountDownLatch selfEnterActionLatch = mock(CountDownLatch.class);

        b = builder.withState(SuperSimpleStates.B);
        c = builder.withState(SuperSimpleStates.C);

        b.withSelfEnterAction((ctx, data) -> selfEnterActionLatch.countDown());
        b.withEnterAction((ctx, data) -> enterActionLatch.countDown());

        a.transitionTo(SuperSimpleStates.B).onEvent(String.class);
        b.transitionTo(SuperSimpleStates.B).onEvent(String.class).withGuard("self"::equals);
        b.transitionTo(SuperSimpleStates.H).onEvent(String.class).withGuard("exit"::equals);
        b.transitionTo(SuperSimpleStates.C).onEvent(String.class).withGuard("C"::equals);
        c.transitionTo(SuperSimpleStates.B).onEvent(String.class).withGuard("B"::equals);


        go("start");

        // self should NOT have been called at this point but the "regular" enter action should have been.
        verify(selfEnterActionLatch, never()).countDown();
        verify(enterActionLatch).countDown();

        fsm.onEvent("self");

        // Now the self should have been triggered but the regular not so that one is still at one invocation
        verify(selfEnterActionLatch).countDown();
        verify(enterActionLatch).countDown();

        // two self and then B -> C -> B, which should not trigger the "self" but the regular on Enter...
        fsm.onEvent("self");
        fsm.onEvent("self");
        fsm.onEvent("C");
        fsm.onEvent("B");
        verify(selfEnterActionLatch, times(3)).countDown();
        verify(enterActionLatch, times(2)).countDown();

        // another "self" and then we're out.
        fsm.onEvent("self");
        fsm.onEvent("exit");
        assertThat(fsm.isTerminated(), is(true));

        verify(selfEnterActionLatch, times(4)).countDown();
        verify(enterActionLatch, times(2)).countDown();
    }


    /**
     * You are only allowed to have a transformation on a transition that is going to a transient state.
     * Make sure this is true...
     *
     */
    @Test(expected = IllegalTransformationOnTransitionException.class)
    public void testIllegalTransformationOnTransition() throws Exception {
        b = builder.withState(SuperSimpleStates.B);
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class).withTransformation(String::length);
        addDefaultTransitions(b);
        build();
    }

    /**
     * For transitions going to a transient state, we can optionally specify a transformation
     * on that transition. In the test below, we have a simple chain where State A, on a String event, will transition
     * to State B (which is a transient state) and convert that original String to an integer. State B will then
     * have a transition to the terminal State H when it receives an Integer.
     *
     * Hence, if successful, we'll have the following flow:
     *
     * <pre>
     *     A --- String ---> B ---- convert String to Integer ---> H
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testTransformingStateTransition() throws Exception {
        b = builder.withTransientState(SuperSimpleStates.B);

        final AtomicInteger result = new AtomicInteger(0);
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class).withTransformation(Integer::parseInt);

        b.transitionTo(SuperSimpleStates.H).onEvent(Integer.class).withAction(i -> {
            result.set(i);
            latch.countDown();;
        });
        b.transitionTo(SuperSimpleStates.H).asDefaultTransition(); // just because we need to specify a default transition on a transient state.

        go("5");
        ensureLatches(latch);
        assertThat(fsm.isTerminated(), is(true));
        assertThat(result.get(), is(5));
    }

    /**
     * In this case we will have two transient states, B, & C  and of course, everything starts off with
     * the initial State A. A will transition to B when A receives a String and will on that transformation
     * append "world" to the String. C will on a String, transition to the terminal state H and transform it to an integer
     * by counting the number of characters.
     *
     * Hence, if successful, we'll have the following flow:
     *
     * <pre>
     *     A --- String + " world" ---> B ---- count chars ---> C --- action: latch open & store count -> H
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testTwoTransientStatesWithTransformation() throws Exception {
        b = builder.withTransientState(SuperSimpleStates.B);
        c = builder.withTransientState(SuperSimpleStates.C);

        final AtomicInteger result = new AtomicInteger(0);
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class).withTransformation(s -> s + "world");
        b.transitionTo(SuperSimpleStates.C).onEvent(String.class).withTransformation(String::length);
        c.transitionTo(SuperSimpleStates.H).onEvent(Integer.class).withAction(i -> {
            result.set(i);
            latch.countDown();
        });

        addDefaultTransitions(b, c);

        go("hello ");
        ensureLatches(latch);
        assertThat(fsm.isTerminated(), is(true));
        assertThat(result.get(), is("hello world".length()));
    }

    /**
     * The cool thing with transient states and transformations on transitions to
     * those states is that you can build up a decision tree (well) and have it
     * execute all the way through. We are testing that ability.
     *
     * @throws Exception
     */
    @Test
    public void testBranchesInTransientStatesWithTransformations() throws Exception {
        final AtomicInteger accumulator = new AtomicInteger(0);
        final List<Enum<SuperSimpleStates>> transitions = buildBranchStateMachine(accumulator);

        // first test should take us A -> B -> C -> H
        go("one");
        assertThat(fsm.isTerminated(), is(true));
        assertStatesVisited(transitions, SuperSimpleStates.A, SuperSimpleStates.B, SuperSimpleStates.C, SuperSimpleStates.H);


        // this time around we have a string that's longer than 5 characters so we should go to
        // C directly. That transition will transform the string into an integer, which then will
        // match the transition to D. D then has two matches for Integer but one with a guard where the value
        // has to be larger than 20... otherwise it'll match the other one... note that the two actions
        // are different on those as well...
        //
        // so, the final count for the accumulation of all int transitions should be
        // 31 (the length of the original string and the original transition) + 31 (because we match the transition
        // of D -> H where the value is larger than 20 and at that point, we just add it to the accumulated value)
        transitions.clear(); // clean-up from previous...
        accumulator.set(0);
        go("string longer than 5 characters");
        assertThat(fsm.isTerminated(), is(true));
        assertThat(accumulator.get(), is(31 + 31));
        assertStatesVisited(transitions, SuperSimpleStates.A, SuperSimpleStates.C, SuperSimpleStates.D, SuperSimpleStates.H);


        // ok so same as above but now we have string that is larger than 5 still but shorter than 20,
        // which means the final transition from D -> H should now match the other Integer event where
        // the action is actually to subtract from the original, which will yield zero
        // Note: the actual states we traversed are still the same...
        transitions.clear(); // clean-up from previous...
        accumulator.set(0);
        go("more than 5");
        assertThat(fsm.isTerminated(), is(true));
        assertThat(accumulator.get(), is(0));
        assertStatesVisited(transitions, SuperSimpleStates.A, SuperSimpleStates.C, SuperSimpleStates.D, SuperSimpleStates.H);
    }

    /**
     * This is part of the test {@link #testBranchesInTransientStatesWithTransformations()} just
     * to keep things a bit cleaner...
     *
     */
    private List<Enum<SuperSimpleStates>> buildBranchStateMachine(final AtomicInteger accumulator) {
        b = builder.withTransientState(SuperSimpleStates.B);
        c = builder.withTransientState(SuperSimpleStates.C);
        d = builder.withTransientState(SuperSimpleStates.D);
        e = builder.withTransientState(SuperSimpleStates.E);

        final List<Enum<SuperSimpleStates>> transitions = new ArrayList<>();

        // A will transition to B if the String is less than 5 characters otherwise it will transition
        // to C and convert the string to an Integer by counting the length of the String.
        a.transitionTo(SuperSimpleStates.B).onEvent(String.class).withGuard(s -> s.length() < 5);
        a.transitionTo(SuperSimpleStates.C).onEvent(String.class).withTransformation(String::length);

        // B will, on a String, transition to C and transform that string to, well, another string and append some stuff to it.
        b.transitionTo(SuperSimpleStates.D).onEvent(String.class).withGuard(s -> s.startsWith("help")).withTransformation(s -> "no help for you");
        b.transitionTo(SuperSimpleStates.C).onEvent(String.class).withTransformation(s -> s + " padding stuff");

        c.transitionTo(SuperSimpleStates.E).onEvent(String.class).withGuard(s -> s.startsWith("hello"));
        c.transitionTo(SuperSimpleStates.H).onEvent(String.class);
        c.transitionTo(SuperSimpleStates.D).onEvent(Integer.class).withAction(accumulator::addAndGet);

        d.transitionTo(SuperSimpleStates.H).onEvent(String.class);
        d.transitionTo(SuperSimpleStates.H).onEvent(Integer.class).withGuard(i -> i > 20).withAction(accumulator::addAndGet);
        d.transitionTo(SuperSimpleStates.H).onEvent(Integer.class).withAction(i -> accumulator.set(accumulator.get() - i));

        e.transitionTo(SuperSimpleStates.H).asDefaultTransition();

        addDefaultTransitions(b, c, d);
        addEntryState(transitions, a, b, c, d, e, h);

        return transitions;
    }


    /**
     * This helper method is part of the
     * @param transitions
     */
    private void testBranchesVersion1(final List<Enum<SuperSimpleStates>> transitions) {

    }

    private void assertStatesVisited(final List<Enum<SuperSimpleStates>> list, final Enum<SuperSimpleStates>... expected) {
        System.err.println(list);
        assertThat(list.size(), is(expected.length));
        for (int i = 0; i < list.size(); ++i) {
            assertThat(list.get(i), is(expected[i]));
        }
    }

    private void addEntryState(final List<Enum<SuperSimpleStates>> list, final StateBuilder<SuperSimpleStates, Context, Data>... states) {
        Arrays.stream(states).forEach(state -> {
            state.withEnterAction((c, d) -> {
                synchronized (list) {
                    list.add(state.getState());
                };
            });
        });

    }

    /**
     * Every transient state must have a default transition associated with it.
     * Most often that is not what we are testing so this convenience method is just to add
     * that default transition and will always go to the terminal state H.
     * @param states
     */
    private void addDefaultTransitions(final StateBuilder<SuperSimpleStates, Context, Data>... states) {
        Arrays.stream(states).forEach(state -> {
            state.transitionTo(SuperSimpleStates.H).asDefaultTransition();
        });
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

}
