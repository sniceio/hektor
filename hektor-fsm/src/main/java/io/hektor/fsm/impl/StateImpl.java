package io.hektor.fsm.impl;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author jonas@jonasborjesson.com
 */
public class StateImpl<S extends Enum<S>, C extends Context, D extends Data> implements State<S, C, D> {

    private final S state;

    private final boolean isInitial;
    private final boolean isFinal;
    private final List<Transition<?, S, C, D>> transitions;
    private final Optional<BiConsumer<C, D>> enterAction;
    private final Optional<BiConsumer<C, D>> exitAction;

    public StateImpl(final S state,
                     final boolean isInitial,
                     final boolean isFinal,
                     final List<Transition<?, S, C, D>> transitions,
                     final BiConsumer<C, D> enterAction,
                     final BiConsumer<C, D> exitAction) {
        this.state = state;
        this.isInitial = isInitial;
        this.isFinal = isFinal;
        this.transitions = transitions;
        this.enterAction = Optional.ofNullable(enterAction);
        this.exitAction = Optional.ofNullable(exitAction);
    }

    @Override
    public S getState() {
        return state;
    }

    public Optional<BiConsumer<C, D>> getEnterAction() {
        return enterAction;
    }

    public Optional<BiConsumer<C, D>> getExitAction() {
        return exitAction;
    }

    public boolean isInital() {
        return isInitial;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public Optional<Transition<? extends Object, S, C, D>> accept(final Object event) {
        return transitions.stream().filter(t -> t.match(event)).findFirst();
    }

}
