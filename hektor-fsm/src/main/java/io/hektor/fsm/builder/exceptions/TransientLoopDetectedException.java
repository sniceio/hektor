package io.hektor.fsm.builder.exceptions;

/**
 * If you would be able to specify a transition from a transient state to another transient state (or itself)
 * you will likely end up in a loop. When the FSM is being built, this scenario is checked and if detected
 * this exception will be thrown.
 *
 * Note: if you were to have an action along with the transition then I guess you could mutate some internal
 * state variable and as such break the loop but that is too hard to guarantee so for now we will simply
 * just not allow it. That includes a transient state A having a transition to another transient state B.
 * In this last case one could argue unless the transient state B then loops back to A we should allow it, but again,
 * too complicated to keep track of and I wonder if there is a real need for it. If it is, we'll revisit.
 *
 * @author jonas@jonasborjesson.com
 */
public class TransientLoopDetectedException extends StateBuilderException {

   public TransientLoopDetectedException(final Enum state) {
       super(state, "A transient state cannot loop back to itself since you will have an infinite loop");
   }

}
