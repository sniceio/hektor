package io.hektor.fsm.builder.exceptions;

/**
 * Indicates that you tried to define transitions on a final state. A final state cannot have
 * transitions since that state is, well, final. As in, once the FSM reaching this state, it's
 * dead, no way back.
 *
 * @author jonas@jonasborjesson.com
 */
public class FinalStateTransitionsException extends StateBuilderException {

   public FinalStateTransitionsException(final Enum state) {
       super(state, "A final state cannot have transitions");
   }

}
