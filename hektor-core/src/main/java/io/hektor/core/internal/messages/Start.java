package io.hektor.core.internal.messages;

/**
 * Internal message to indicate that an actor should be started.
 *
 * @author jonas@jonasborjesson.com
 */
public class Start {

    public static final Start MSG = new Start();

    private Start() {}
}
