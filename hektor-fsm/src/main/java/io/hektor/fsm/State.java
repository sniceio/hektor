package io.hektor.fsm;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface State<S extends Enum<S>, C extends Context, D extends Data> {

    /**
     * Retrieve the enum value of the state.
     */
    S getState();

    /**
     * Check whether or not this state is initial.
     */
    boolean isInital();

    /**
     * Check whether or not this state is final.
     */
    boolean isFinal();

    /**
     * Check whether or not this state is a transient state.
     */
    boolean isTransient();

    /**
     * See if this {@link State} would accept the given event.
     *
     * @param event the event to see if it is accepted by this state or not.
     * @return if the {@link State} accepts the event, then an the {@link Transition}
     * that ultimatelely was the one accepting the event will be returned. If not, an empty
     * {@link Optional} will be returned.
     */
    Optional<Transition<? extends Object, S, C, D>> accept(Object event);

    Optional<BiConsumer<C, D>> getEnterAction();

    Optional<BiConsumer<C, D>> getExitAction();

}
