package io.hektor.fsm.builder.exceptions;

import io.hektor.fsm.State;
import io.hektor.fsm.Transition;

import java.util.function.Function;

/**
 * You can optionally specify a transformation with your transition but you can only do so
 * if the state you are transitioning to is a so-called transient state.
 *
  * See {@link io.hektor.fsm.builder.TransitionBuilder#withTransformation(Function)}
 */
public class IllegalTransformationOnTransitionException extends FSMBuilderException {
    private static final String msg = "You cannot specify a transformation on a " +
                    "transition that does not transition to a non-transient state. You tried to go from %s " +
                    "to %s and %s isn't a transient state";

    public IllegalTransformationOnTransitionException(final Enum from, final Enum to) {
        super(String.format(msg, from, to, to));
    }
}
