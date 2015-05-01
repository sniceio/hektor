package io.hektor.core.internal.workerexecutor;

import io.hektor.config.DispatcherConfiguration;
import io.hektor.config.WorkerThreadExecutorConfig;
import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.internal.ActorBox;
import io.hektor.core.internal.InternalDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<ActorPath, ActorBox> actors = new ConcurrentHashMap<>(100);

    public DefaultDispatcher(final String name, final DispatcherConfiguration config) {
        this.name = name;

        Optional<WorkerThreadExecutorConfig> optional = config.workerThreadExecutorConfig();
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("TODO: shouldn't throw an exception and perhaps we want a different dispatcher impl");
        }

        WorkerThreadExecutorConfig c = optional.get();
        executorService = c.getExecutorService();
        noOfWorkers = c.getNoOfWorkers();
        workerQueue = new BlockingQueue[noOfWorkers];
        workers = new Worker[noOfWorkers];
        for (int i = 0; i < noOfWorkers; ++i) {
            workerQueue[i] = new ArrayBlockingQueue<Runnable>(100);
            workers[i] = new Worker(i, workerQueue[i]);
            executorService.submit(workers[i]);
        }
    }

    @Override
    public void register(final ActorBox actor) {
        final ActorPath path = actor.ref().path();
        if (actors.putIfAbsent(path, actor) != null) {
            System.err.println("oh my, actor already existed");
        }
        System.err.println("actor has been registered");
    }

    @Override
    public void unregister(final ActorBox actor) {

    }

    @Override
    public void dispatch(final ActorRef sender, final ActorRef receiver, final Object msg) {
        if (msg == null) {
            return;
        }

        System.err.println("Dispatching");
        final BlockingQueue<Runnable> queue = workerQueue[Math.abs(receiver.path().hashCode()) % noOfWorkers];
        final Runnable dispatchJob = new Runnable() {
            @Override
            public void run() {
                final ActorBox boxed = actors.get(receiver.path());
                final Actor actor = boxed.actor();
                actor.onReceive(msg);
            }
        };

        if (!queue.offer(dispatchJob)) {
            System.err.println("oh man, queue is full");
        }

    }

    private int hashKey(final Object msg) {
        return msg.hashCode();
    }

    private static class Worker implements Runnable {

        /**
         * Just a simple id of the worker. Mainly used for logging/debugging
         * purposes.
         */
        private final int id;

        private final BlockingQueue<Runnable> queue;

        /**
         *
         */
        public Worker(final int id, final BlockingQueue<Runnable> queue) {
            this.id = id;
            this.queue = queue;
        }

        @Override
        public void run() {
            System.err.println(id + " Worker starting");
            final List<Runnable> jobs = new ArrayList<>(10);
            while (true) {
                try {

                    // use take as
                    final Runnable event = queue.take();
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
                }
            }
        }

    }
}
