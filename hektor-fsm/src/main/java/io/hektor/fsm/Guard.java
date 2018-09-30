package io.hektor.fsm;

import java.util.function.Predicate;

/**
 * A specialized {@link Predicate} that accepts three arguments. The event
 * itself, the FSM {@link Context} and the {@link Data} object in case you need
 * to make decisions on more than just the event.
 *
 */
@FunctionalInterface
public interface Guard<E, C extends Context, D extends Data> {

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param event the incoming event that triggers this guard associated with a particular {@link FSM}.
     * @param context the {@link Context} associated with the {@link FSM} for which this {@link Guard} is associated with.
     * @param data the {@link Data} bag associated with the {@link FSM} for which this {@link Guard} is associated with.
     *
     * @return {@code true} if the input arguments matches the predicate,
     * otherwise {@code false}
     */
    boolean test(E event, C context, D data);
}
