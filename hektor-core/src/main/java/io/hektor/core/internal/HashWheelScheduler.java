package io.hektor.core.internal;

import io.hektor.core.ActorRef;
import io.hektor.core.Cancellable;
import io.hektor.core.Scheduler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Simple wrapper around the netty HashWheelScheduler.
 *
 * @author jonas@jonasborjesson.com
 */
public class HashWheelScheduler implements Scheduler {

    private HashedWheelTimer timer;

    public HashWheelScheduler() {
        timer = new HashedWheelTimer();
    }

    @Override
    public Cancellable schedule(final Object msg, final ActorRef receiver, final ActorRef sender, final Duration delay) {
        final Task task = new Task(msg, receiver, sender);
        final Timeout timeout = timer.newTimeout(task, delay.toMillis(), TimeUnit.MILLISECONDS);
        return new CancellableTask(timeout);
    }

    private static class CancellableTask implements Cancellable {
        private final Timeout timeout;

        public CancellableTask(final Timeout timeout) {
            this.timeout = timeout;
        }

        @Override
        public boolean cancel() {
            return timeout.cancel();
        }
    }

    private static class Task implements TimerTask {

        private final Object msg;
        private final ActorRef receiver;
        private final ActorRef sender;

        public Task(final Object msg, final ActorRef receiver, final ActorRef sender) {
            this.msg = msg;
            this.receiver = receiver;
            this.sender = sender;
        }

        @Override
        public void run(final Timeout timeout) throws Exception {
            receiver.tell(msg, sender);
        }
    }

}
