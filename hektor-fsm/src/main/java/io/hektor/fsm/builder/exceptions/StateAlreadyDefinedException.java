package io.hektor.fsm.builder.exceptions;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class StateAlreadyDefinedException extends StateBuilderException {

   public StateAlreadyDefinedException(final Enum state) {
       super(state);
   }

}
