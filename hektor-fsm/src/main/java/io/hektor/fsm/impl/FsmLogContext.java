package io.hektor.fsm.impl;

import io.snice.logging.Context;

import java.util.function.BiConsumer;

public record FsmLogContext<S extends Enum<S>>(String friendlyName,
                                               Object uuid,
                                               S currentState,
                                               S nextState,
                                               Object event) implements Context {

    private static final String FSM_UUID_LABEL = "fsm_uuid";
    private static final String FSM_LABEL = "fsm_name";
    private static final String CURRENT_STATE_LABEL = "current_state";
    private static final String NEXT_STATE_LABEL = "next_state";
    private static final String EVENT_TYPE_LABEL = "event_type";
    private static final String EVENT_LABEL = "event";

    public FsmLogContext(final String friendlyName, final Object uuid) {
        this(friendlyName, uuid, null, null, null);
    }

    public FsmLogContext transition(final S currentState, final S nextState, final Object event) {
        return new FsmLogContext(friendlyName, uuid, currentState, nextState, event);
    }

    public FsmLogContext unhandledEvent(final S currentState, final Object event) {
        return new FsmLogContext(friendlyName, uuid, currentState, null, event);
    }

    @Override
    public void copyContext(final BiConsumer<String, String> visitor) {

        visitor.accept(FSM_UUID_LABEL, uuid.toString());
        visitor.accept(FSM_LABEL, friendlyName);
        visitor.accept(CURRENT_STATE_LABEL, (currentState == null ? null : currentState.toString()));
        visitor.accept(NEXT_STATE_LABEL, (nextState == null ? null : nextState.toString()));

        if (event != null) {
            visitor.accept(EVENT_TYPE_LABEL, event.getClass().getName());
            visitor.accept(EVENT_LABEL, format(event));
        } else {
            visitor.accept(EVENT_TYPE_LABEL, null);
            visitor.accept(EVENT_LABEL, null);
        }
    }

    /**
     * TODO: should probably allow to pass in their own formatting logic.
     */
    public static String format(final Object object) {
        return object.toString();
    }
}
