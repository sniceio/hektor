package io.hektor.fsm.builder;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;
import io.hektor.fsm.impl.StateImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author jonas@jonasborjesson.com
 */
public class StateBuilder<S extends Enum<S>, C extends Context, D extends Data> {
    private final S state;

    private boolean isInitialState;
    private boolean isFinalState;

    private final List<TransitionBuilder<?, S, C, D>> transitions = new ArrayList<>();

    private BiConsumer<C, D> enterAction;
    private BiConsumer<C, D> exitAction;

    public StateBuilder(final S state) {
        this.state = state;
    }

    /**
     * Register an action that will be executed upon entering this state.
     *
     * @return
     */
    public StateBuilder<S, C, D> withEnterAction(final BiConsumer<C, D> action) {
        enterAction = action;
        return this;
    }

    /**
     * Register an action that will be executed upon exiting this state.
     *
     * @return
     */
    public StateBuilder<S, C, D> withExitAction(final BiConsumer<C, D> action) {
        exitAction = action;
        return this;
    }

    /**
     * Set the maximum time we allow to stay in this state. When this duration
     * has passed and we are still in this state, the event StateTimeoutEvent will be
     * fired and given to the state machine. If there are no registered transitions
     * associated with that event for this state, then
     *
     * TODO - yeah, what do we want to happen. Perhaps just call the onUnhandledEvent or something?
     * so that there is no difference between this one and everything else.
     *
     * @return
     */
    public StateBuilder<S, C, D> withStateTimeout() {
        return this;
    }

    public EventBuildStep<S, C, D> transitionTo(final S state) {
        return new EventBuildStep<S, C, D>() {

            @Override
            public <E> TransitionBuilder<E, S, C, D> onEvent(final Class<E> event) {
                final TransitionBuilder<E, S, C, D> builder = new TransitionBuilder<>(state, event);
                transitions.add(builder);
                return builder;
            }
        };
    }

    public State build() {

        if (isFinalState && !transitions.isEmpty()) {
            throw new StateBuilderException(state, "A final state cannot have transitions");
        }

        if (!isFinalState && transitions.isEmpty()) {
            throw new StateBuilderException(state, "You must specify at least one transition for non-final states");
        }

        final List<Transition<?, S, C, D>> ts = transitions.stream().map(TransitionBuilder::build).collect(Collectors.toList());
        return new StateImpl(state, isInitialState, isFinalState, ts, enterAction, exitAction);
    }

    public StateBuilder<S, C, D> isInital(final boolean value) {
        isInitialState = value;
        return this;
    }

    public boolean isInital() {
        return isInitialState;
    }

    public StateBuilder<S, C, D> isFinal(final boolean value) {
        isFinalState = value;
        return this;
    }

    public boolean isFinal() {
        return isFinalState;
    }
}
