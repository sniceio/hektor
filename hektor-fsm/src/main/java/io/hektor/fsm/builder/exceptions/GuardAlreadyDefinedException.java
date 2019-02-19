package io.hektor.fsm.builder.exceptions;

import io.hektor.fsm.State;
import io.hektor.fsm.Transition;

/**
 * Indicates that a guard was already associated with the object ({@link State} or
 * {@link Transition})
 */
public class GuardAlreadyDefinedException extends FSMBuilderException {
    public GuardAlreadyDefinedException() {
        // left empty intentionally
    }

    public GuardAlreadyDefinedException(final String msg) {
        super(msg);
    }
}
