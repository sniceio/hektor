package io.hektor.config;

import io.hektor.core.Hektor;

import java.util.Map;

/**
 * Helper class to configure {@link Hektor}.
 */
public class HektorConfigurator {

    public static Hektor defaultHektor(final String name) {
        return Hektor.withName(name).withConfiguration(defaultHektorConfig()).build();
    }

    private static HektorConfiguration defaultHektorConfig() {
        final var conf = new HektorConfiguration();

        /*
        hektor:
        dispatchers:
        my-dispatcher:
        executor: worker-thread-executor
        workerThreadExecutor:
        noOfWorkers: 4
        throughput: 75
         */
        final var dispatcherConf = new DispatcherConfiguration.Builder()
                .withExecutor("worker-thread-executor")
                .withThroughput(75)
                .withWorkerThreadExecutor(new WorkerThreadExecutorConfig.Builder().withNoOfWorkers(4).build())
                .build();

        final var dispatchers = Map.of("default-dispatcher", dispatcherConf);
        conf.dispatchers(dispatchers);
        return conf;
    }
}
