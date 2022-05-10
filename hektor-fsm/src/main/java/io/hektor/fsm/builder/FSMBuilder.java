package io.hektor.fsm.builder;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Definition;
import io.hektor.fsm.State;
import io.hektor.fsm.builder.exceptions.FSMBuilderException;
import io.hektor.fsm.builder.exceptions.FinalStateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.IllegalTransformationOnTransitionException;
import io.hektor.fsm.builder.exceptions.InitialStateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.StateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.StateNotDefinedException;
import io.hektor.fsm.builder.exceptions.TransientLoopDetectedException;
import io.hektor.fsm.builder.impl.StateBuilderImpl;
import io.hektor.fsm.impl.DefinitionImpl;
import io.hektor.fsm.impl.StateImpl;

import java.util.Arrays;

/**
 * @author jonas@jonasborjesson.com
 */
public class FSMBuilder<S extends Enum<S>, C extends Context, D extends Data> {

    private final StateBuilderImpl<S, C, D>[] states;

    public FSMBuilder(final S[] possibleStates) {
        states = new StateBuilderImpl[possibleStates.length];
    }

    public StateBuilder<S, C, D> withInitialState(final S state) {
        if (hasInitialState()) {
            throw new InitialStateAlreadyDefinedException(state);
        }

        final StateBuilderImpl<S, C, D> builder = defineState(state, false);
        builder.isInital(true);
        return builder;
    }

    public StateBuilder<S, C, D> withFinalState(final S state) {
        if (hasFinalState()) {
            throw new FinalStateAlreadyDefinedException(state);
        }
        final StateBuilderImpl<S, C, D> builder = defineState(state, false);
        builder.isFinal(true);
        return builder;
    }

    private boolean hasFinalState() {
        return Arrays.stream(states).filter(b -> b != null && b.isFinal()).findFirst().isPresent();
    }

    private boolean hasInitialState() {
        return Arrays.stream(states).filter(b -> b != null && b.isInital()).findFirst().isPresent();
    }

    public StateBuilder<S, C, D> withState(final S state) throws StateAlreadyDefinedException {
        return defineState(state, false);
    }

    public StateBuilder<S, C, D> withTransientState(final S state) throws StateAlreadyDefinedException {
        return defineState(state, true);
    }

    private StateBuilderImpl<S, C, D> defineState(final S state, final boolean isTransient) throws StateAlreadyDefinedException {
        final StateBuilderImpl<S, C, D> builder = new StateBuilderImpl<>(state, isTransient);
        if (states[state.ordinal()] != null) {
            throw new StateAlreadyDefinedException(state);
        }

        states[state.ordinal()] = builder;
        return builder;
    }

    /**
     * Build the definition of the state machine and validate that all is well.
     *
     * The following rules will be validate upon building the definition:
     *
     * <ul>
     *     <li></li>
     *     <li></li>
     *     <li></li>
     *     <li>Transitions with transformations can only be going to transient states and anything else is
     *     not allowed, hence, we will throw an exception.</li>
     * </ul>
     * @return
     * @throws FSMBuilderException in case any issues with the FSM is detected.
     */
    public Definition<S, C, D> build() throws FSMBuilderException {
        if (!hasInitialState()) {
            throw new FSMBuilderException(FSMBuilderException.ErrorCode.NO_INITIAL_STATE);
        }

        if (!hasFinalState()) {
            throw new FSMBuilderException("FSM is missing a final state");
        }

        final StateImpl<S, C, D>[] states = new StateImpl[this.states.length];
        for (int i = 0; i < states.length; ++i) {
            final StateBuilderImpl<S, C, D> builder = this.states[i];
            states[i] = builder != null ? (StateImpl)builder.build() : null;
        }

        checkTransitions(states);
        return new DefinitionImpl(states);
    }

    /**
     * Check so that all transitions are actually valid. E.g, need to
     * ensure that all transitions are going to states that
     * actually exists.
     * <p>
     * Furthermore, we need to ensure that two transient states are not directly
     * connected, which currently isn't allowed.
     *
     * @param states
     */
    private void checkTransitions(final StateImpl<S, C, D>[] states) throws TransientLoopDetectedException {
        for (final StateImpl<S, C, D> state : states) {
            if (state == null) {
                continue;
            }

            state.getConnectedNodes().forEach(s -> {
                final State toState = (State)states[s.ordinal()];
                if (toState == null) {
                    throw new StateNotDefinedException(s);
                }

                // if it's not a transient state, ensure that none of the transitions to this
                // state doesn't have a transformation on it since it would be useless on a non-transient
                // state and as such, it is forbidden (Because it would just be confusing for a user otherwise)
                if (!toState.isTransient() && state.getTransitionsToState(s).stream().filter(t -> t.getTransformation().isPresent()).findFirst().isPresent()) {
                    throw new IllegalTransformationOnTransitionException(state.getState(), toState.getState());
                }

                if (state.isTransient() && toState.isTransient()) {

                    // ok, definitely forbidden. A transient goes back to itself so
                    // very likely be a loop
                    if (state.equals(toState)) {
                        throw new TransientLoopDetectedException(state.getState());
                    }

                    // TODO: we need to be able to detect if three transient states
                    // are connected. E.g., transient states A, B, C are connected
                    // A -> B -> C -> A then that could potentially be bad.
                    // probably need to build a graph and to some graph analysis
                    // on it...
                }
            });

        }
    }

}
