package io.hektor.fsm.builder.exceptions;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author jonas@jonasborjesson.com
 */
public class FSMBuilderException extends RuntimeException {

    /**
     * There are many different issues that can occur when building the state machine
     * and instead of having individual exceptions for everything, we have these error
     * codes. The reason is that some of the issues that may arise may be hard to understand
     * and generic exceptions will not catch those scenarios so it is important that we have
     * enough documentation around those cases so the user, you, understand why building the
     * FSM failed.
     */
    public enum ErrorCode {

        /**
         * Every FSM must have an initial state or we do not know where to start off with.
         */
        NO_INITIAL_STATE(1000, "FSM is missing an initial state"),

        /**
         * Every FSM must have its final state defined.
         */
        NO_FINAL_STATE(1001, "FSM is missing a final state"),

        /**
         * You can optionally specify a transformation with your transition but you can only do so
         * if the state you are transitioning to is a so-called transient state.
         *
         * See {@link io.hektor.fsm.builder.TransitionBuilder#withTransformation(Function)}
         */
        ILLEGAL_TRANSFORMATION_ON_TRANSITION(1100, "You cannot specify a transformation on a " +
                "transition that does not transition to a non-transient state. You tried to go from %s " +
                "to %s and %s isn't a transient state");


        private final int code;
        private final String template;

        ErrorCode(final int code, final String template) {
            this.code = code;
            this.template = template;
        }

        public String getTemplate() {
            return "ErrorCode " + code + ": " + template;
        }

    }

    private final Optional<ErrorCode> error;

    public FSMBuilderException() {
        // left empty intentionally
        error = Optional.empty();
    }

    public FSMBuilderException(final String msg) {
        super(msg);
        error = Optional.empty();
    }

    public FSMBuilderException(final ErrorCode error, final Object... args) {
        super(String.format(error.getTemplate(), args));
        this.error = Optional.of(error);
    }

    public Optional<ErrorCode> getErrorCode() {
        return error;
    }

}
