package io.hektor.fsm.impl;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.FSM;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;
import io.hektor.fsm.TransitionListener;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.ensureNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public class FsmImpl<S extends Enum<S>, C extends Context, D extends Data> implements FSM<S, C, D> {
    private final Object uuid;
    private final BiConsumer<S, Object> unhandledEventHandler;
    private final TransitionListener<S> transitionListener;
    private final State[] states;
    private final boolean[] hasEnteredState;
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

        // keep track of whether we have ever entered a given
        // state, which is needed to keep track of whether or not we
        // should execute the initial enter action, which is only done the
        // very first time you enter a state.
        hasEnteredState = new boolean[states.length];
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
        // TODO: catch all if action throws exception

        // only execute the initial enter action the very first time you
        // enter the state.
        if (!hasEnteredState[currentState.getState().ordinal()]) {
            final Optional<BiConsumer<C, D>> initialAction = currentState.getInitialEnterAction();
            initialAction.ifPresent(a -> a.accept(ctx, data));
            hasEnteredState[currentState.getState().ordinal()] = true;
        }

        final Optional<BiConsumer<C, D>> action = currentState.getEnterAction();
        action.ifPresent(a -> a.accept(ctx, data));
    }

    private void exitCurrentState() {
        final Optional<BiConsumer<C, D>> action = currentState.getExitAction();
        action.ifPresent(a -> a.accept(ctx, data));
    }

    @Override
    public final void onEvent(final Object event) {
        final Optional<Transition<Object, S, C, D>> optional = currentState.accept(event, ctx, data);
        if (optional.isPresent()) {
            final Transition<Object, S, C, D> transition = optional.get();
            transition(transition, event);

            // our builders is supposed to guarantee that we don't end up in a loop,
            // which is a risk with transient states.
            if (currentState.isTransient()) {
                handleTransientTransition(transition, event);
            }

        } else {
            if (unhandledEventHandler != null) {
                unhandledEventHandler.accept((S) currentState.getState(), event);
            }
        }
    }

    private void handleTransientTransition(final Transition<Object, S, C, D> transition, final Object event ) {
        try {
            final Optional<Function<Object, ?>> transform = transition.getTransformation();
            final Object transformedEvent = transform.isPresent() ? transform.get().apply(event) : event;
            onEvent(transformedEvent);
        } catch (final Throwable t) {
            // TODO: what is the strategy for if the transformation throws an exception?
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
            } else {
                // "self" transition. I.e. State B -> State B
                final Optional<BiConsumer<C, D>> action = currentState.getSelfEnterAction();
                action.ifPresent(a -> a.accept(ctx, data));
            }

        } catch(final Throwable t) {
            // TODO: HEKTOR-9 - and it is quite bad if the current state is a transient
            // state and the "exitCurrentState" is the one throwing the exception
            // then we'll end up in a loop
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
