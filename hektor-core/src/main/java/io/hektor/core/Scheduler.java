package io.hektor.core;

import java.time.Duration;
import java.util.function.Supplier;

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

    /**
     * Schedule a message to be sent to the receiving actor after a certain delay. The message
     * that will be sent is produced by the {@link Supplier}.
     *
     * Note that the {@link Supplier} will only be called once the duration is up. As such, if this
     * task is cancelled, the {@link Supplier} will never be called.
     *
     * @param producer a {@link Supplier} that will produce a message that will be delivered to the
     *                 receiver.
     * @param receiver the receiving actor.
     * @param sender the sending actor.
     * @param delay the delay before sending off the msg.
     * @return a {@link Cancellable} representing this outstanding task.
     */
    <T> Cancellable schedule(Supplier<T> producer, ActorRef receiver, ActorRef sender, Duration delay);
}
