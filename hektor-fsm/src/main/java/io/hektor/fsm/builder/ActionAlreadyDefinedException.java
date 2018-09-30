package io.hektor.fsm.builder;

import io.hektor.fsm.State;
import io.hektor.fsm.Transition;

/**
 * Indicates that an action was already associated with the object ({@link State} or
 * {@link Transition})
 */
public class ActionAlreadyDefinedException extends FSMBuilderException {
    public ActionAlreadyDefinedException() {
        // left empty intentionally
    }

    public ActionAlreadyDefinedException(final String msg) {
        super(msg);
    }
}
