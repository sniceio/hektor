package io.hektor.fsm;

import java.util.function.BiConsumer;

public interface Definition<S extends Enum<S>, C extends Context, D extends Data> {

    /**
     * Create a new {@link FSM} based off of this {@link Definition} and assign
     * a unique UUID to this state machine. The FSM itself doesn't actually use the
     * UUID other but may be important for the applications who actually are using
     * this FSM.
     *
     * Also pass in a handler for any events that doesn't match the FSM. This allow you
     * to take action if there are any events that has been missed when creating the
     * FSM. A common best practice is to at the very least log on WARN so your systems
     * can alert you to the fact that your FSM is missing a transition.
     *
     * @param uuid
     * @param context
     * @param data
     * @param onUnhandledEvent
     * @return
     */
    FSM newInstance(Object uuid, C context, D data, BiConsumer<S, Object> onUnhandledEvent, TransitionListener<S> transitionListener);

    /**
     *
     * @param uuid
     * @return
     */
    FSM newInstance(Object uuid, C context, D data);

}
