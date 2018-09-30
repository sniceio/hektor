package io.hektor.fsm.impl;

import java.util.Optional;
import java.util.function.BiConsumer;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.FSM;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;
import io.hektor.fsm.TransitionListener;

import static io.hektor.fsm.PreConditions.ensureNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public class FsmImpl<S extends Enum<S>, C extends Context, D extends Data> implements FSM<S, C, D> {
    private final Object uuid;
    private final BiConsumer<S, Object> unhandledEventHandler;
    private final TransitionListener<S> transitionListener;
    private final State[] states;
    private final S initialState;
    private final C ctx;
    private final D data;

    private State currentState;

    public FsmImpl(final Object uuid,
                   final State[] states,
                   final S initialState,
                   final C ctx,
                   final D data,
                   final BiConsumer<S, Object> unhandledEventHandler,
                   final TransitionListener<S> transitionListener) {
        this.uuid = uuid;
        this.states = states;
        this.initialState = initialState;
        this.ctx = ctx;
        this.data = data;
        this.unhandledEventHandler = unhandledEventHandler;
        this.transitionListener = transitionListener;
    }

    @Override
    public final boolean isStarted() {
        return currentState != null;
    }

    @Override
    public final boolean isTerminated() {
        return currentState != null && currentState.isFinal();
    }

    @Override
    public final void start() {
        if (currentState != null) {
            return;
        }

        enterState(initialState);
    }

    @Override
    public void reStartAndEnter(final S state) throws IllegalArgumentException {
        ensureNotNull(state);
        enterState(state);
    }

    private void enterState(final S state) {
        currentState = states[state.ordinal()];
        final Optional<BiConsumer<C, D>> action = currentState.getEnterAction();

        // TODO: catch all if action throws exception
        action.ifPresent(a -> a.accept(ctx, data));

    }

    private void exitCurrentState() {
        final Optional<BiConsumer<C, D>> action = currentState.getExitAction();
        action.ifPresent(a -> a.accept(ctx, data));
    }

    @Override
    public final void onEvent(final Object event) {
        final Optional<Transition<Object, S, C, D>> optional = currentState.accept(event);
        if (optional.isPresent()) {
            transition(optional.get(), event);
        } else {
            if (unhandledEventHandler != null) {
                unhandledEventHandler.accept((S) currentState.getState(), event);
            }
        }
    }

    /**
     * Execute the transition.
     * @param transition
     * @param event
     */
    private void transition(final Transition<Object, S, C, D> transition, final Object event) {
        try {
            final S toState = transition.getToState();
            invokeTransitionListener((S) currentState.getState(), toState, event);

            // Note: our builders will ensure that there is only one action
            //       associated with our transition.
            transition.getAction().ifPresent(action -> action.accept(event));
            transition.getStatefulAction().ifPresent(action -> action.accept(event, ctx, data));
            if (currentState.getState() != toState) {
                exitCurrentState();
                enterState(toState);
            }
        } catch(final Throwable t) {
            // TODO: HEKTOR-9
            t.printStackTrace();
        }
    }

    private void invokeTransitionListener(final S fromState, final S toState, final Object event) {
        if (transitionListener == null) {
            return;
        }

        try {
            transitionListener.onTransition(fromState, toState, event);
        } catch (final Throwable t) {
            // TODO: log on warning etc.
        }
    }

    @Override
    public final S getState() {
        return (S)currentState.getState();
    }
}
