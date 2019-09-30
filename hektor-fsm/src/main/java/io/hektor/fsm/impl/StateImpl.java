package io.hektor.fsm.impl;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author jonas@jonasborjesson.com
 */
public class StateImpl<S extends Enum<S>, C extends Context, D extends Data> implements State<S, C, D> {

    private final S state;

    private final boolean isInitial;
    private final boolean isFinal;
    private final boolean isTransient;
    private final List<Transition<?, S, C, D>> transitions;
    private final Optional<Transition<?, S, C, D>> defaultTransition;
    private final Optional<BiConsumer<C, D>> enterAction;
    private final Optional<BiConsumer<C, D>> exitAction;

    private final List<S> connectedNodes = new ArrayList<>();

    public StateImpl(final S state,
                     final boolean isInitial,
                     final boolean isFinal,
                     final boolean isTransient,
                     final List<Transition<?, S, C, D>> transitions,
                     final Optional<Transition<?, S, C, D>> defaultTransition,
                     final BiConsumer<C, D> enterAction,
                     final BiConsumer<C, D> exitAction) {
        this.state = state;
        this.isInitial = isInitial;
        this.isFinal = isFinal;
        this.isTransient = isTransient;
        this.transitions = transitions;
        this.defaultTransition = defaultTransition;
        this.enterAction = Optional.ofNullable(enterAction);
        this.exitAction = Optional.ofNullable(exitAction);

        transitions.forEach(this::markConnectedNode);
        markConnectedNode(defaultTransition.orElse(null));
    }

    /**
     * We need to keep track of what other states we are connected to because when
     * we validate the FSM upon build time, there are certain transitions that isn't
     * allowed.
     *
     * Note that this only happens when you build the FSM, which you will only really do
     * once (so don't confuse this with instantiating the FSM)
     * @param transition
     */
    private void markConnectedNode(final Transition<?, S, C, D> transition) {
        if (transition == null) {
            return;
        }

        final S toState = transition.getToState();
        if (!connectedNodes.contains(toState)) {
            connectedNodes.add(toState);
        }
    }

    public List<Transition<?, S, C, D>> getTransitionsToState(final S state) {
        final List<Transition<?, S, C, D>> ts = transitions.stream().filter(t -> t.getToState() == state).collect(Collectors.toList());
        defaultTransition.ifPresent(d -> {
            if (d.getToState() == state) {
                ts.add(d);
            }
        });

        return ts;

    };

    @Override
    public S getState() {
        return state;
    }

    @Override
    public Optional<BiConsumer<C, D>> getEnterAction() {
        return enterAction;
    }

    @Override
    public Optional<BiConsumer<C, D>> getExitAction() {
        return exitAction;
    }

    @Override
    public boolean isInital() {
        return isInitial;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public List<S> getConnectedNodes() {
        return Collections.unmodifiableList(connectedNodes);
    }

    @Override
    public Optional<Transition<? extends Object, S, C, D>> accept(final Object event, final C ctx, final D data) {
        final Optional<Transition<? extends Object, S, C, D>> optional = transitions.stream().filter(t -> t.match(event, ctx, data)).findFirst();
        if (optional.isPresent()) {
            return optional;
        }

        return defaultTransition;
    }
}
