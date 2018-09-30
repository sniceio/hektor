package io.hektor.fsm;

import java.util.function.BiConsumer;

/**
 * Represents an action to be performed in the context of a
 * FSM. This is really just a specialization of the generic {@link BiConsumer}
 * that takes three arguments instead of two and also locks down the upper bound
 * of two of the arguments.
 */
@FunctionalInterface
public interface Action<E, C extends Context, D extends Data> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param event the incoming event that triggers this action associated with a particular {@link FSM}.
     * @param context the {@link Context} associated with the {@link FSM} for which this {@link Action} is associated with.
     * @param data the {@link Data} bag associated with the {@link FSM} for which this {@link Action} is associated with.
     */
    void accept(E event, C context, D data);
}
