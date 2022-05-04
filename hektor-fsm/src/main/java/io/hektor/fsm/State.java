package io.hektor.fsm;

import java.util.List;
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
     * Retrieve a list of all other states this state is connected to.
     * This is just the "forward" connections. I.e., if A is connected to B, then
     * A would return a list of one element with B in it. However, B doesn't really
     * know that A is connected to it so if you'd ask B for all its connections, then
     * A would not be part of that list (unless of course you actually specified that
     * on the B state explicitly)
     *
     * @return a list of states this state is connected to.
     */
    List<S> getConnectedNodes();

    /**
     * See if this {@link State} would accept the given event.
     *
     * @param event the event to see if it is accepted by this state or not.
     * @return if the {@link State} accepts the event, then an the {@link Transition}
     * that ultimatelely was the one accepting the event will be returned. If not, an empty
     * {@link Optional} will be returned.
     */
    Optional<Transition<? extends Object, S, C, D>> accept(Object event, C ctx, D data);

    Optional<BiConsumer<C, D>> getInitialEnterAction();

    Optional<BiConsumer<C, D>> getSelfEnterAction();

    Optional<BiConsumer<C, D>> getEnterAction();

    Optional<BiConsumer<C, D>> getExitAction();

}
