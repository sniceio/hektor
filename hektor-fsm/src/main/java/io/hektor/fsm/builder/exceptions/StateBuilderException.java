package io.hektor.fsm.builder.exceptions;

/**
 * @author jonas@jonasborjesson.com
 */
public class StateBuilderException extends FSMBuilderException {
    final Enum state;

    public StateBuilderException(final Enum state) {
        this.state = state;
    }

    public StateBuilderException(final Enum state, final String message) {
        super(message);
        this.state = state;
    }

    /**
     * Get the state that had already been defined.
     *
     * @return
     */
    public Enum getState() {
        return state;
    }
}
