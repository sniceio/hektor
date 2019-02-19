package io.hektor.fsm.builder.exceptions;

/**
 * Indicates that you tried to define an initial state twice.
 *
 * @author jonas@jonasborjesson.com
 */
public class InitialStateAlreadyDefinedException extends StateBuilderException {

   public InitialStateAlreadyDefinedException(final Enum state) {
       super(state);
   }

}
