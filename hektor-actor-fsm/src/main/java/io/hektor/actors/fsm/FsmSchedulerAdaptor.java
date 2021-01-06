package io.hektor.actors.fsm;

import io.hektor.core.ActorRef;
import io.hektor.fsm.Cancellable;
import io.hektor.fsm.Scheduler;

import java.time.Duration;
import java.util.function.Supplier;

public class FsmSchedulerAdaptor implements Scheduler {

    private final io.hektor.core.Scheduler scheduler;
    private final ActorRef self;

    public FsmSchedulerAdaptor(final io.hektor.core.Scheduler scheduler, final ActorRef self) {
        this.scheduler = scheduler;
        this.self = self;
    }

    @Override
    public <T> Cancellable schedule(final Supplier<T> producer, final Duration delay) {
        final var timeout = scheduler.schedule(producer, self, self, delay);
        return new CancellableTask<T>(timeout);
    }

    @Override
    public <T> Cancellable schedule(final T msg, final Duration delay) {
        final var timeout = scheduler.schedule(msg, self, self, delay);
        return new CancellableTask<T>(timeout);
    }

    public static class CancellableTask<T> implements Cancellable {

        private final io.hektor.core.Cancellable actualCancellable;

        public CancellableTask(final io.hektor.core.Cancellable actualCancellable) {
            this.actualCancellable = actualCancellable;
        }

        @Override
        public boolean cancel() {
            return actualCancellable.cancel();
        }
    }

}
