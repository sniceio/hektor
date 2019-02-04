package io.hektor.core.internal.workerexecutor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.hektor.config.DispatcherConfiguration;
import io.hektor.config.WorkerThreadExecutorConfig;
import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.internal.ActorStore;
import io.hektor.core.internal.InternalDispatcher;
import io.hektor.core.internal.InternalHektor;
import io.hektor.core.internal.InvokeActorTask;
import io.snice.preconditions.PreConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultDispatcher implements InternalDispatcher {

    /**
     * This is the name of the dispatcher as configured
     * by the user. Mainly used for logging.
     */
    private final String name;

    /**
     * This is the root path of the entire actor system.
     * It is primarily used for building up relative paths
     * when a user is asking to find a specific actor.
     */
    private final ActorPath root;

    private final ExecutorService executorService;
    private final BlockingQueue<Runnable>[] workerQueue;
    // private final BlockingDeque<Runnable>[] workerQueue;
    private final Worker[] workers;
    private final int noOfWorkers;
    private final ActorStore actorStore;
    private final MetricRegistry metricRegistry;
    private final InternalHektor hektor;

    /**
     * A metric timer for keeping track of the time a task stays in the
     * worker queue before it is being processed. There is one timer per
     * worker queue
     */
    private final Timer[] inQueueLatency;

    public DefaultDispatcher(final String name,
                             final ActorPath root,
                             final InternalHektor hektor,
                             final ActorStore actorStore,
                             final MetricRegistry metricRegistry,
                             final DispatcherConfiguration config) {
        this.name = name;
        this.root = root;
        this.actorStore = actorStore;
        this.hektor = hektor;
        this.metricRegistry = metricRegistry;

        final Optional<WorkerThreadExecutorConfig> optional = config.workerThreadExecutorConfig();
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("TODO: shouldn't throw an exception and perhaps we want a different dispatcher impl");
        }

        WorkerThreadExecutorConfig c = optional.get();
        executorService = c.getExecutorService();
        noOfWorkers = c.getNoOfWorkers();
        workerQueue = new BlockingQueue[noOfWorkers];
        // workerQueue = new BlockingDeque[noOfWorkers];
        workers = new Worker[noOfWorkers];
        inQueueLatency = new Timer[noOfWorkers];
        for (int i = 0; i < noOfWorkers; ++i) {
            final String timerName = MetricRegistry.name(Worker.class, "worker", Integer.toString(i), "latency");
            inQueueLatency[i] = metricRegistry.timer(timerName);

            final String jobTimerName = MetricRegistry.name(Worker.class, "worker", Integer.toString(i), "job");
            final Timer jobTimer = metricRegistry.timer(jobTimerName);

            workerQueue[i] = new ArrayBlockingQueue<Runnable>(100);
            // workerQueue[i] = new LinkedBlockingDeque<>(100);
            workers[i] = new Worker(i, workerQueue[i], inQueueLatency[i], jobTimer);
            // final Thread t = new Thread(workers[i]);
            // t.start();
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
    public void dispatch(final ActorRef sender, final ActorRef receiver, final Object msg) {
        if (msg == null) {
            return;
        }
        internalDispatch(sender, receiver, msg, null);
    }

    public void internalDispatch(final ActorRef sender, final ActorRef receiver, final Object msg, final CompletableFuture<Object> askFuture) {

        final BlockingQueue<Runnable> queue = workerQueue[Math.abs(receiver.path().hashCode()) % noOfWorkers];
        // final BlockingDeque<Runnable> queue = workerQueue[Math.abs(receiver.path().hashCode()) % noOfWorkers];
        final InvokeActorTask task = InvokeActorTask.create(hektor, sender, receiver, msg, askFuture);
        if (!queue.offer(task)) {
            System.err.println("oh man, queue is full");
        }
    }

    @Override
    public CompletableFuture<Object> ask(final ActorRef sender, final ActorRef receiver, final Object msg) {
        assertNotNull(msg, "If you are asking something you cannot pass in an empty message");
        final CompletableFuture<Object> askFuture = new CompletableFuture<>();
        internalDispatch(sender, receiver, msg, askFuture);
        return askFuture;
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
            final int drain = 5;
            final List<Runnable> jobs = new ArrayList<>(drain);
            while (true) {
                Timer.Context timerContext = null;
                try {
                    // use take as
                    final Runnable event = queue.take();

                    timerContext = jobTimer.time();
                    event.run();

                    final int noOfJobs = this.queue.drainTo(jobs, drain);
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
