package io.hektor.fsm.builder.exceptions;

/**
 * Indicates that you didn't define any transitions for the given state.
 * All states, except the final state, must have at least one transition
 * defined. Otherwise your FSM isn't complete and would get "stuck".
 *
 * @author jonas@jonasborjesson.com
 */
public class TransitionMissingException extends StateBuilderException {

   public TransitionMissingException(final Enum state) {
       super(state, "You must specify at least one transition for non-final states");
   }

}
