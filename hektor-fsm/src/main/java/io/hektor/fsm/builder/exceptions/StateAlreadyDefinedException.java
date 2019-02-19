package io.hektor.fsm.builder.exceptions;

/**
 * You can only define the same state once. If you try again, you will get this
 * exception thrown in your face
 *
 * @author jonas@jonasborjesson.com
 */
public class StateAlreadyDefinedException extends StateBuilderException {

   public StateAlreadyDefinedException(final Enum state) {
       super(state);
   }

}
