package io.hektor.core.internal.workerexecutor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.hektor.config.DispatcherConfiguration;
import io.hektor.config.WorkerThreadExecutorConfig;
import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.internal.ActorBox;
import io.hektor.core.internal.ActorStore;
import io.hektor.core.internal.DefaultActorPath;
import io.hektor.core.internal.InternalDispatcher;
import io.hektor.core.internal.InvokeActorTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultDispatcher implements InternalDispatcher {

    private final String name;
    private final ExecutorService executorService;
    private final BlockingQueue<Runnable>[] workerQueue;
    private final Worker[] workers;
    private final int noOfWorkers;
    private final ActorStore actorStore;
    private final MetricRegistry metricRegistry;

    /**
     * A metric timer for keeping track of the time a task stays in the
     * worker queue before it is being processed. There is one timer per
     * worker queue
     */
    private final Timer[] inQueueLatency;

    public DefaultDispatcher(final String name,
                             final ActorStore actorStore,
                             final MetricRegistry metricRegistry,
                             final DispatcherConfiguration config) {
        this.name = name;
        this.actorStore = actorStore;
        this.metricRegistry = metricRegistry;

        Optional<WorkerThreadExecutorConfig> optional = config.workerThreadExecutorConfig();
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("TODO: shouldn't throw an exception and perhaps we want a different dispatcher impl");
        }

        WorkerThreadExecutorConfig c = optional.get();
        executorService = c.getExecutorService();
        noOfWorkers = c.getNoOfWorkers();
        workerQueue = new BlockingQueue[noOfWorkers];
        workers = new Worker[noOfWorkers];
        inQueueLatency = new Timer[noOfWorkers];
        for (int i = 0; i < noOfWorkers; ++i) {
            final String timerName = MetricRegistry.name(Worker.class, "worker", Integer.toString(i), "latency");
            inQueueLatency[i] = metricRegistry.timer(timerName);

            final String jobTimerName = MetricRegistry.name(Worker.class, "worker", Integer.toString(i), "job");
            final Timer jobTimer = metricRegistry.timer(jobTimerName);

            workerQueue[i] = new ArrayBlockingQueue<Runnable>(100);
            workers[i] = new Worker(i, workerQueue[i], inQueueLatency[i], jobTimer);
            executorService.submit(workers[i]);
        }
    }

    @Override
    public void register(final ActorRef ref, final Actor actor) {
        actorStore.store(ref, actor);
    }

    @Override
    public void unregister(final ActorRef ref) {
        actorStore.remove(ref);
    }

    @Override
    public Optional<ActorBox> lookup(final ActorRef ref) {
        return actorStore.lookup(ref);
    }

    @Override
    public Optional<ActorBox> lookup(final ActorPath path) {
        return actorStore.lookup(path);
    }

    @Override
    public Optional<ActorBox> lookup(final String path) throws  IllegalArgumentException {
        if (path != null && !path.isEmpty() && path.charAt(0) != '/') {
            throw new IllegalArgumentException("You must specify an absolute reference to the actor");
        }
        return actorStore.lookup(DefaultActorPath.create(null, path));
    }

    @Override
    public void dispatch(final ActorRef sender, final ActorRef receiver, final Object msg) {
        if (msg == null) {
            return;
        }

        // tomorrow, if we pin an actor we could just override the hash code here...
        final BlockingQueue<Runnable> queue = workerQueue[Math.abs(receiver.path().hashCode()) % noOfWorkers];
        final InvokeActorTask task = InvokeActorTask.create(this, sender, receiver, msg);
        if (!queue.offer(task)) {
            System.err.println("oh man, queue is full");
        }
    }

    private static class Worker implements Runnable {

        /**
         * Just a simple id of the worker. Mainly used for logging/debugging
         * purposes.
         */
        private final int id;

        private final BlockingQueue<Runnable> queue;

        private final Timer queueLatencyTimer;

        /**
         *
         */
        private final Timer jobTimer;

        /**
         *
         */
        public Worker(final int id, final BlockingQueue<Runnable> queue, final Timer queueLatencyTimer, final Timer jobtimer) {
            this.id = id;
            this.queue = queue;
            this.queueLatencyTimer = queueLatencyTimer;
            this.jobTimer = jobtimer;
        }

        @Override
        public void run() {
            System.err.println(id + " Worker starting");
            final List<Runnable> jobs = new ArrayList<>(10);
            while (true) {
                Timer.Context timerContext = null;
                try {
                    // use take as
                    final Runnable event = queue.take();

                    timerContext = jobTimer.time();
                    event.run();

                    final int noOfJobs = this.queue.drainTo(jobs, 10);
                    for (int i = 0; i < noOfJobs; ++i) {
                        final Runnable job = jobs.get(i);
                        job.run();
                    }
                    jobs.clear();
                } catch (final Throwable t) {
                    // do something cool
                    t.printStackTrace();
                } finally {
                    if (timerContext != null) {
                        timerContext.stop();
                    }
                }
            }
        }

    }
}
