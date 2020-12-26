package io.hektor.actors.fsm;

import io.hektor.actors.Alert;

/**
 * Generic {@link Alert} codes for an FSM.
 */
public enum FsmAlertCode implements Alert {

    /**
     * If the {@link io.hektor.fsm.FSM} receives an event for which there
     * is not defined transition, we will log the following warning. Any event that is "unhandled"
     * is a bug and should be addressed asap.
     *
     * The arguments to the formatting string below are:
     * <ol>
     *     <li>FSM State</li>
     *     <li>Class name of the event</li>
     *     <li>Event as a formatted string</li>
     * </ol>
     */
    UNHANDLED_FSM_EVENT(1000, "{} Unhandled event of type {}. Formatted output {}");

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
