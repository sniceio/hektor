package io.hektor.fsm;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Scheduler {


    /**
     * Schedule to deliver an event to the {@link FSM} at some later
     * point in time. The event that will be delivered is the one
     * created by the supplied producer. I.e., when the time is up,
     * the producer will be called and the returned object will
     * be given to the {@link FSM} that scheduled this event.
     *
     * NOTE: a small note regarding who will guarantee that the above
     * statement is true. I.e., both to actually run a scheduler and then
     * to guarantee that the calling FSM is the one that receives it, well,
     * that depends on the runtime environment where hektor-fsm is running.
     *
     * The raw hektor-fsm actually doesn't provide a runtime environment
     * so if you were to use the raw fsm framework, you will have to implement
     * and guarantee this.
     *
     * Or, if you don't feel like it, pick a runtime environment provided by
     * the rest of the hektor family, such as hektor-fsm-actor.
     *
     * @param producer
     * @param delay
     * @return
     */
    <T> Cancellable schedule(Supplier<T> producer, Duration delay);
}
