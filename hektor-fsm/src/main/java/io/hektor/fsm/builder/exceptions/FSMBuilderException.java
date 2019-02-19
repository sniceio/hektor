package io.hektor.fsm.builder.exceptions;

/**
 * @author jonas@jonasborjesson.com
 */
public class FSMBuilderException extends RuntimeException {

    public FSMBuilderException() {
        // left empty intentionally
    }

    public FSMBuilderException(final String msg) {
        super(msg);
    }

}
