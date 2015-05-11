package io.hektor.core.internal.messages;

/**
 * Internal message to indicate that an actor should be stopped.
 *
 * @author jonas@jonasborjesson.com
 */
public class Stop {

    public static final Stop MSG = new Stop();

    private Stop() {}
}
