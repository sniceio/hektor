package io.hektor.fsm.builder;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;
import io.hektor.fsm.builder.exceptions.DefaultTransitionAlreadySpecifiedException;
import io.hektor.fsm.builder.exceptions.FinalStateIsTransientException;
import io.hektor.fsm.builder.exceptions.FinalStateTransitionsException;
import io.hektor.fsm.builder.exceptions.TransientLoopDetectedException;
import io.hektor.fsm.builder.exceptions.TransientStateMissingTransitionException;
import io.hektor.fsm.builder.exceptions.TransitionMissingException;
import io.hektor.fsm.impl.StateImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    /**
     * There must be a default transition defined, which is a transition that
     * accepts any event and has no guards. I.e., it is guaranteed it will
     * be executed, which is necessary for transition states.
     */
    private TransitionBuilder<Object, S, C, D> defaultTransition;

    private BiConsumer<C, D> enterAction;
    private BiConsumer<C, D> exitAction;

    /**
     * Whether or not this is a transient state.
     */
    private final boolean isTransient;

    public StateBuilder(final S state) {
        this(state, false);
    }

    public StateBuilder(final S state, final boolean isTransient) {
        this.state = state;
        this.isTransient = isTransient;
    }

    public S getState() {
        return state;
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

    public EventBuildStep<S, C, D> transitionToSelf() {
        return transitionTo(state);
    }

    public EventBuildStep<S, C, D> transitionTo(final S state) {
        return new EventBuildStep<S, C, D>() {

            @Override
            public <E> TransitionBuilder<E, S, C, D> onEvent(final Class<E> event) {
                ensureTransientTransition(state);
                final TransitionBuilder<E, S, C, D> builder = new TransitionBuilder<>(state, event);
                transitions.add(builder);
                return builder;
            }

            @Override
            public DefaultTransitionBuilder<Object, S, C, D> asDefaultTransition() {
                if (defaultTransition != null) {
                    throw new DefaultTransitionAlreadySpecifiedException(state);
                }

                ensureTransientTransition(state);

                final TransitionBuilder<Object, S, C, D> builder = new TransitionBuilder<>(state, Object.class, true);
                defaultTransition = builder;
                return builder;
            }
        };
    }

    /**
     * A transient state cannot transition back to itself since, most likely, a loop will occur.
     * One could perhaps mutate some state on the action associated with the transition and as
     * such break the loop but it's too hard to figure out so for now, thiis is not allowed.
     *
     * @param toState
     */
    private void ensureTransientTransition(final S toState) {
        if (isTransient && state == toState) {
            throw new TransientLoopDetectedException(state);
        }
    }

    public State build() {

        if (isFinalState && !transitions.isEmpty()) {
            throw new FinalStateTransitionsException(state);
        }

        if (!isFinalState && transitions.isEmpty() && defaultTransition == null) {
            throw new TransitionMissingException(state);
        }

        // you shouldn't be able to specify this but in case we introduce a bug
        // that breaks the builder pattern
        if (isFinalState && isTransient) {
            throw new FinalStateIsTransientException(state);
        }

        if (isTransient && defaultTransition == null) {
            throw new TransientStateMissingTransitionException(state);
        }

        final List<Transition<?, S, C, D>> ts = transitions.stream().map(TransitionBuilder::build).collect(Collectors.toList());
        final Optional<Transition<Object, S, C, D>> defaultTs = Optional.ofNullable(defaultTransition == null ? null : defaultTransition.build());
        return new StateImpl(state, isInitialState, isFinalState, isTransient, ts, defaultTs, enterAction, exitAction);
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
