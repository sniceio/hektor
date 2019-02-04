package io.hektor.core.internal.messages;

/**
 * Internal message to indicate that an actor should be started.
 *
 * @author jonas@jonasborjesson.com
 */
public class Watch {

    public static final Watch MSG = new Watch();

    private Watch() {}
}
