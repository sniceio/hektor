package io.hektor.fsm.builder.exceptions;

/**
 * Indicates that you defined a transient state but didn't specify any default
 * transitions, which you must for a transient state or you will get stuck
 * in that state and then it wouldn't be a transient state, now would it!
 *
 * @author jonas@jonasborjesson.com
 */
public class TransientStateMissingTransitionException extends StateBuilderException {

   public TransientStateMissingTransitionException(final Enum state) {
       super(state, "A transient state must have a default transition specified");
   }

}
