package io.hektor.fsm.builder.exceptions;

/**
 * If you define a transition from A to B but then you never end up defining the state B,
 * you will get this exception thrown when trying to build the FSM.
 *
 * @author jonas@jonasborjesson.com
 */
public class StateNotDefinedException extends StateBuilderException {

   public StateNotDefinedException(final Enum state) {
       super(state, "The state you are trying to transition to has not been defined");
   }

}
