package io.hektor.fsm;

/**
 *
 */
@FunctionalInterface
public interface TransitionListener<S extends Enum<S>> {

    /**
     * Will be invoked just before the {@link Transition} is executed by
     * the {@link FSM}. If this method throws an exception, it will be ignored
     * and the {@link Transition} will still be executed. However, if an exception
     * is thrown, it will be logged on WARN and you really should check what on earth
     * you are doing.
     *
     * @param currentState the state we are transitioning from, i.e. the current state.
     * @param toState      the state we will be transitioning to, which could be the same as the
     *                     current state.
     * @param event        the event that triggere this transition.
     */
    void onTransition(S currentState, S toState, Object event);
}
