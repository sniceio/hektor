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
     * Register an action that will be executed upon entering this state the very first time only.
     *
     * Note that any "regular" enter actions, as registered through {@link #withEnterAction(BiConsumer)}, will
     * always be executed, including the very first time you enter the state. The initial enter action
     * will always be executed first.
     *
     * Example:
     * You have a state B that has registered an initial enter action and a "regular" enter action and
     * your application makes the following transitions:
     *
     * <code>A -> B -> C -> B -> D</code>
     *
     * I.e., from the state A, you enter B for the very first time (given some event, not shown and not important
     * for this example). Since this is the very first time you enter B the initial enter action will be executed
     * first (if there is one of course), followed by the "regular" enter action (if there is one).
     *
     * The next transition between C and back to B is no longer the first time you enter the state B and as such,
     * ONLY the "regular" enter action (if there is one) will be executed.
     *
     * Also remember that if you go from B -> B that doesn't trigger the enter/exit actions, which is why, in the
     * above example, have to "fully" leave B and then come back via C again in order to trigger the enter actions.
     *
     * @param action
     * @return
     */
    StateBuilder<S, C, D> withInitialEnterAction(final BiConsumer<C, D> action);

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
