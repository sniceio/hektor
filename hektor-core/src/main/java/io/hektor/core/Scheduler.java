package io.hektor.core;

import java.time.Duration;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Scheduler {

    /**
     * Schedule the message to be sent to the receiving actor after a certain delay.
     *
     * @param msg the message to send over to the receiver.
     * @param receiver the receiving actor.
     * @param sender the sending actor.
     * @param delay the delay before sending off the msg.
     */
    Cancellable schedule(Object msg, ActorRef receiver, ActorRef sender, Duration delay);
}
