package io.hektor.fsm.builder;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Definition;
import io.hektor.fsm.State;
import io.hektor.fsm.builder.exceptions.FSMBuilderException;
import io.hektor.fsm.builder.exceptions.FinalStateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.InitialStateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.StateAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.StateNotDefinedException;
import io.hektor.fsm.builder.exceptions.TransientLoopDetectedException;
import io.hektor.fsm.impl.DefinitionImpl;

import java.util.Arrays;

/**
 * @author jonas@jonasborjesson.com
 */
public class FSMBuilder<S extends Enum<S>, C extends Context, D extends Data> {

    private final StateBuilder<S, C, D>[] states;

    public FSMBuilder(final S[] possibleStates) {
        states = new StateBuilder[possibleStates.length];
    }

    public StateBuilder<S, C, D> withInitialState(final S state) {
        if (hasInitialState()) {
            throw new InitialStateAlreadyDefinedException(state);
        }

        final StateBuilder<S, C, D> builder = defineState(state, false);
        builder.isInital(true);
        return builder;
    }

    public StateBuilder<S, C, D> withFinalState(final S state) {
        if (hasFinalState()) {
            throw new FinalStateAlreadyDefinedException(state);
        }
        final StateBuilder<S, C, D> builder = defineState(state, false);
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

    private StateBuilder<S, C, D> defineState(final S state, final boolean isTransient) throws StateAlreadyDefinedException {
        final StateBuilder<S, C, D> builder = new StateBuilder<>(state, isTransient);
        if (states[state.ordinal()] != null) {
            throw new StateAlreadyDefinedException(state);
        }

        states[state.ordinal()] = builder;
        return builder;
    }

    public Definition<S, C, D> build() throws FSMBuilderException {
        if (!hasInitialState()) {
            throw new FSMBuilderException("FSM is missing an initial state");
        }

        if (!hasFinalState()) {
            throw new FSMBuilderException("FSM is missing a final state");
        }

        final State<S, C, D>[] states = new State[this.states.length];
        for (int i = 0; i < states.length; ++i) {
            final StateBuilder<S, C, D> builder = this.states[i];
            states[i] = builder != null ? builder.build() : null;
        }

        // TODO: need to check so that all transitions are going
        // to states that actually exists.

        checkTransitions(states);
        return new DefinitionImpl(states);
    }

    /**
     * Check so that all transitions are actually valid. E.g,, need to
     * ensure that all transitions are going to states that
     * actually exists.
     * <p>
     * Furthermore, we need to ensure that two transient states are not directly
     * connected, which currently isn't allowed.
     *
     * @param states
     */
    private void checkTransitions(final State<S, C, D>[] states) throws TransientLoopDetectedException {
        for (final State<S, C, D> state : states) {
            if (state == null) {
                continue;
            }

            state.getConnectedNodes().forEach(s -> {
                final State toState = states[s.ordinal()];
                if (toState == null) {
                    throw new StateNotDefinedException(s);
                }

                if (state.isTransient() && toState.isTransient()) {
                    throw new TransientLoopDetectedException(state.getState());
                }
            });
        }
    }
}
