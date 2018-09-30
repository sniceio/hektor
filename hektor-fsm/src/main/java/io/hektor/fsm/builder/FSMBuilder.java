package io.hektor.fsm.builder;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Definition;
import io.hektor.fsm.State;
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

        final StateBuilder<S, C, D> builder = withState(state);
        builder.isInital(true);
        return builder;
    }

    public StateBuilder<S, C, D> withFinalState(final S state) {
        if (hasFinalState()) {
            throw new FinalStateAlreadyDefinedException(state);
        }
        final StateBuilder<S, C, D> builder = withState(state);
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
        final StateBuilder<S, C, D> builder = new StateBuilder<>(state);
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

        final State[] states = new State[this.states.length];
        for (int i = 0; i < states.length; ++i) {
            final StateBuilder<S, C, D> builder = this.states[i];
            states[i] = builder != null ? builder.build() : null;
        }

        // TODO: need to check so that all transitions are going
        // to states that actually exists.

        return new DefinitionImpl(states);
    }
}
