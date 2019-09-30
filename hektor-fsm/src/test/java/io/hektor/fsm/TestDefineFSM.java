package io.hektor.fsm;

import io.hektor.fsm.builder.FSMBuilder;
import io.hektor.fsm.builder.exceptions.FinalStateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.InitialStateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.StateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.StateNotDefinedException;
import io.hektor.fsm.builder.impl.StateBuilderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.hektor.fsm.SimpleFsmStates.DEAD;
import static io.hektor.fsm.SimpleFsmStates.DONE;
import static io.hektor.fsm.SimpleFsmStates.INIT;
import static io.hektor.fsm.SimpleFsmStates.WORKING;

/**
 * @author jonas@jonasborjesson.com
 */
public class TestDefineFSM {

    private FSMBuilder<SimpleFsmStates, Context, Data> builder;

    @Before
    public void setUp() {
        builder = FSM.of(SimpleFsmStates.class).ofContextType(Context.class).withDataType(Data.class);

    }

    @Test
    public void testDefineStateTwice() {
        builder.withState(INIT);
        builder.withState(WORKING);

        try {
            builder.withState(WORKING);
            Assert.fail("Expected a " + StateAlreadyDefinedException.class);
        } catch (final StateAlreadyDefinedException e) {
            // TODO: check so that the correct state is in the exception
        }
    }

    /**
     * Ensure that we cannot define the initial or the final state twice.
     *
     * NOTE: the reason I don't use @Test(expected = ...) is that if I do that
     * and the first withInitialState throws the exception, the test will still
     * pass but that is in fact wrong.
     */
    @Test
    public void testDefineInitialOrFinalStateTwice() {
        builder.withInitialState(INIT);
        builder.withState(WORKING);
        builder.withFinalState(DEAD);

        try {
            builder.withInitialState(INIT);
            Assert.fail("Expected a " + InitialStateAlreadyDefinedException.class);
        } catch (final InitialStateAlreadyDefinedException e) {
            // TODO: check so that the correct state is in the exception
        }

        try {
            builder.withFinalState(DEAD);
            Assert.fail("Expected a " + FinalStateAlreadyDefinedException.class);
        } catch (final FinalStateAlreadyDefinedException e) {
            // TODO: check so that the correct state is in the exception
        }
    }

    /**
     * You could define a transition from A to B but then not actually define B. This
     * should be detected and the building of the FSM should fail.
     */
    @Test(expected = StateNotDefinedException.class)
    public void testTransitionToUnDefinedState() {
        final StateBuilderImpl<SimpleFsmStates, Context, Data> init = builder.withInitialState(INIT);
        final StateBuilderImpl<SimpleFsmStates, Context, Data> working = builder.withState(WORKING);
        builder.withFinalState(DEAD);

        init.transitionTo(WORKING).asDefaultTransition();

        // this should then eventually fail when building the FSM
        working.transitionTo(DONE).onEvent(String.class);
        builder.build();
    }

    @Test
    public void testDefineSimpleStateMachine() {
        builder.withInitialState(INIT);
    }
}
