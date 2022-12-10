package io.hektor.fsm.impl;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.hektor.fsm.State;
import io.hektor.fsm.TransitionListener;
import io.hektor.fsm.visitor.FsmVisitor;

import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefinitionImpl<S extends Enum<S>, C extends Context, D extends Data> implements Definition<S, C, D> {

    /**
     * The friendly name is just a way for you to name your FSM to something that makes sense.
     * E.g., you create an FSM for a TCP connection, you may want to name your FSM "tcp" and that
     * friendly name will be part of all logs.
     *
     * Apart from logging, it is not used for anything else.
     */
    private final String friendlyName;
    private final S initialState;
    private final State<S, C, D>[] states;

    /**
     *
     * @param friendlyName
     * @param states
     */
    public DefinitionImpl(final String friendlyName, final State<S, C, D>[] states) {
        this.friendlyName = friendlyName;
        initialState = Arrays.stream(states).filter(s -> s != null && s.isInital()).findFirst().get().getState();
        this.states = states;
    }

    @Override
    public FSM newInstance(final Object uuid, final C ctx, final D data) {
        return newInstance(uuid, ctx, data, null, null);
    }

    @Override
    public FSM newInstance(final Object uuid, final C ctx, final D data, final BiConsumer<S, Object> unhandledEventHandler, final TransitionListener<S> transitionListener) {
        return new FsmImpl(uuid, friendlyName, states, initialState, ctx, data, unhandledEventHandler, transitionListener);
    }

    @Override
    public void acceptVisitor(final FsmVisitor<S, C, D> visitor) {
        Arrays.stream(states).filter(s -> s != null).forEach(s -> s.acceptVisitor(visitor));
    }
}

