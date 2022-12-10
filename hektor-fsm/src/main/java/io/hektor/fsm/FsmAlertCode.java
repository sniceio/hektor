package io.hektor.fsm;

import io.snice.logging.Alert;

/**
 * Generic {@link Alert} codes for an FSM.
 */
public enum FsmAlertCode implements Alert {

    /**
     * If the {@link FSM} receives an event for which there
     * is not defined transition, we will log a warning. Any event that is "unhandled"
     * is a bug and should be addressed asap.
     *
     */
    UNHANDLED_FSM_EVENT(1000, "Unhandled event");

    private final int code;
    private final String msg;

    FsmAlertCode(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }


    @Override
    public String getMessage() {
        return msg;
    }

    @Override
    public int getCode() {
        return code;
    }
}
