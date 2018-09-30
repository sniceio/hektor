package io.hektor.fsm;

import io.hektor.fsm.builder.ContextTypeBuildStep;
import io.hektor.fsm.builder.DataTypeBuildStep;
import io.hektor.fsm.builder.FSMBuilder;

/**
 * @author jonas@jonasborjesson.com
 */
public interface FSM<S extends Enum<S>, C extends Context, D extends Data> {

    /**
     * You must call {@link #start()} on the {@link FSM} before it can be used.
     * This will cause the FSM to "transition" from a null state to its initial state.
     * Any enter actions associated with the initial state will naturally now be called.
     *
     * Note: calling this method when the FSM is already started will silently
     * be ignored.
     */
    void start();

    boolean isStarted();

    /**
     * The FSM is in its terminated state if it has reached the final
     * state.
     *
     * @return
     */
    boolean isTerminated();

    /**
     * Re-start the FSM and enter the specified state. The entry actions associated
     * with this state will be executed. This is useful if your FSM throw an
     * exception and you want to control how to deal with that (and potentially let the
     * FSM start over) or if you have suspended/resumed the FSM (e.g. my serializing it
     * to another JVM) and you want to continue were you left off.
     *
     * @param state
     * @throws IllegalArgumentException in case the state is null
     */
    void reStartAndEnter(S state) throws IllegalArgumentException;

    /**
     * Deliver an event to this {@link FSM}.
     *
     * @param event
     */
    void onEvent(Object event);

    /**
     * The state in which this FSM currently is.
     */
    S getState();

    static <S extends Enum<S>> ContextTypeBuildStep<S> of(final Class<S> type) {
        final S[] possibleStates = type.getEnumConstants();
        return new ContextTypeBuildStep<S>() {
            @Override
            public <C extends Context> DataTypeBuildStep<S, C> ofContextType(final Class<C> type) {
                return new DataTypeBuildStep<S, C>() {
                    @Override
                    public <D extends Data> FSMBuilder<S, C, D> withDataType(final Class<D> type) {
                        return new FSMBuilder<>(possibleStates);
                    }
                };
            }
        };
    }

}