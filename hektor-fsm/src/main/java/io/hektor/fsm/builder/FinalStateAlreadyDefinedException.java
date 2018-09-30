package io.hektor.fsm.builder;

/**
 * Indicates that you tried to define a final state twice.
 *
 * @author jonas@jonasborjesson.com
 */
public class FinalStateAlreadyDefinedException extends StateBuilderException {

   public FinalStateAlreadyDefinedException(final Enum state) {
       super(state);
   }

}
