package io.hektor.fsm.impl;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;
import io.hektor.fsm.docs.Label;
import io.hektor.fsm.visitor.FsmVisitor;

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

    private final Optional<BiConsumer<C, D>> initialEnterAction;
    private final Optional<Label> initialEnterActionLabel;

    private final Optional<BiConsumer<C, D>> selfEnterAction;
    private final Optional<Label> selfEnterActionLabel;

    private final Optional<BiConsumer<C, D>> enterAction;
    private final Optional<Label> enterActionLabel;

    private final Optional<BiConsumer<C, D>> exitAction;
    private final Optional<Label> exitActionLabel;

    private final List<S> connectedNodes = new ArrayList<>();

    public StateImpl(final S state,
                     final boolean isInitial,
                     final boolean isFinal,
                     final boolean isTransient,
                     final List<Transition<?, S, C, D>> transitions,
                     final Optional<Transition<?, S, C, D>> defaultTransition,
                     final BiConsumer<C, D> initialEnterAction,
                     final Label initialEnterActionLabel,
                     final BiConsumer<C, D> selfEnterAction,
                     final Label selfEnterActionLabel,
                     final BiConsumer<C, D> enterAction,
                     final Label enterActionLabel,
                     final BiConsumer<C, D> exitAction,
                     final Label exitActionLabel) {
        this.state = state;
        this.isInitial = isInitial;
        this.isFinal = isFinal;
        this.isTransient = isTransient;
        this.transitions = transitions;
        this.defaultTransition = defaultTransition;
        this.initialEnterAction = Optional.ofNullable(initialEnterAction);
        this.selfEnterAction = Optional.ofNullable(selfEnterAction);
        this.enterAction = Optional.ofNullable(enterAction);
        this.exitAction = Optional.ofNullable(exitAction);

        transitions.forEach(this::markConnectedNode);
        markConnectedNode(defaultTransition.orElse(null));
        this.initialEnterActionLabel = Optional.ofNullable(initialEnterActionLabel);
        this.selfEnterActionLabel = Optional.ofNullable(selfEnterActionLabel);
        this.enterActionLabel = Optional.ofNullable(enterActionLabel);
        this.exitActionLabel = Optional.ofNullable(exitActionLabel);
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
    public void acceptVisitor(final FsmVisitor<S, C, D> visitor) {
        visitor.visit(this);
        transitions.forEach(t -> visitor.visit(state, t));
        defaultTransition.ifPresent(t -> visitor.visit(state, t));
    }

    @Override
    public S getState() {
        return state;
    }

    @Override
    public Optional<BiConsumer<C, D>> getInitialEnterAction() {
        return initialEnterAction;
    }

    @Override
    public Optional<Label> getInitialEnterActionLabel() {
        return initialEnterActionLabel;
    }

    @Override
    public Optional<BiConsumer<C, D>> getSelfEnterAction() {
        return selfEnterAction;
    }

    @Override
    public Optional<Label> getSelfEnterActionLabel() {
        return selfEnterActionLabel;
    }

    @Override
    public Optional<BiConsumer<C, D>> getEnterAction() {
        return enterAction;
    }

    @Override
    public Optional<Label> getEnterActionLabel() {
        return enterActionLabel;
    }

    @Override
    public Optional<BiConsumer<C, D>> getExitAction() {
        return exitAction;
    }

    @Override
    public Optional<Label> getExitActionLabel() {
        return exitActionLabel;
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
