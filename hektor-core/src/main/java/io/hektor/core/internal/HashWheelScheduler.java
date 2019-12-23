package io.hektor.core.internal;

import io.hektor.core.ActorRef;
import io.hektor.core.Cancellable;
import io.hektor.core.Scheduler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Simple wrapper around the netty HashWheelScheduler.
 *
 * @author jonas@jonasborjesson.com
 */
public class HashWheelScheduler implements Scheduler {

    private final HashedWheelTimer timer;

    public HashWheelScheduler() {
        timer = new HashedWheelTimer();
    }

    @Override
    public Cancellable schedule(final Object msg, final ActorRef receiver, final ActorRef sender, final Duration delay) {
        return schedule(() -> msg, receiver, sender, delay);
    }

    @Override
    public <T> Cancellable schedule(final Supplier<T> producer, final ActorRef receiver, final ActorRef sender, final Duration delay) {
        final Task task = new Task(producer, receiver, sender);
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

    private static class Task<T> implements TimerTask {

        private final Supplier<T> supplier;
        private final ActorRef receiver;
        private final ActorRef sender;

        public Task(final Supplier<T> supplier, final ActorRef receiver, final ActorRef sender) {
            this.supplier = supplier;
            this.receiver = receiver;
            this.sender = sender;
        }

        @Override
        public void run(final Timeout timeout) throws Exception {
            try {
                receiver.tell(supplier.get(), sender);
            } catch (final Exception e) {
                // TODO: what to do if the supplier throws an exception?
                throw e;
            }
        }
    }

}
