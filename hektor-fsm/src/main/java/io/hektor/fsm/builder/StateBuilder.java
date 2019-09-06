package io.hektor.fsm.builder;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;

import java.util.function.BiConsumer;

public interface StateBuilder<S extends Enum<S>, C extends Context, D extends Data> {

    Enum<S> getState();

    /**
     * Register an action that will be executed upon entering this state.
     */
    StateBuilder<S, C, D> withEnterAction(final BiConsumer<C, D> action);

    /**
     * Register an action that will be executed upon exiting this state.
     */
    StateBuilder<S, C, D> withExitAction(final BiConsumer<C, D> action);

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
    StateBuilder<S, C, D> withStateTimeout();

    EventBuildStep<S, C, D> transitionToSelf();

    EventBuildStep<S, C, D> transitionTo(final S state);

    State build();
}
