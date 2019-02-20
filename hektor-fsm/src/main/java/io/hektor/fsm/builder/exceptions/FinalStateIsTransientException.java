package io.hektor.fsm.builder.exceptions;

/**
 * Indicates that you tried to define the final state as transient, which you cannot do.
 *
 * @author jonas@jonasborjesson.com
 */
public class FinalStateIsTransientException extends StateBuilderException {

   public FinalStateIsTransientException(final Enum state) {
       super(state, "A final state cannot be marked as transient");
   }

}
