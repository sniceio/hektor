package io.hektor.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
@JsonDeserialize(builder = DispatcherConfiguration.Builder.class)
public class DispatcherConfiguration {

    /**
     * Valid values are:
     * <ul>
     *     <li>worker-thread-executor</li>
     * </ul>
     */
    private final String executor;
    private final int throughput;
    private final WorkerThreadExecutorConfig workerThreadExecutorConfig;

    private DispatcherConfiguration(final String executor,
                                    final int throughput,
                                    final WorkerThreadExecutorConfig workerThreadExecutorConfig) {
        this.executor = executor;
        this.throughput = throughput;
        this.workerThreadExecutorConfig = workerThreadExecutorConfig;
    }

    /**
     * If so configured, the worker-thread-executor configuration is obtained
     * through this method. If not configured, this method will return an empty
     * Optional
     * @return
     */
    public Optional<WorkerThreadExecutorConfig> workerThreadExecutorConfig() {
        return Optional.ofNullable(workerThreadExecutorConfig);
    }

    public String executor() {
        return executor;
    }

    public int throughput() {
        return throughput;
    }

    public static class Builder {

        /**
         * Valid values are:
         * <ul>
         *     <li>worker-thread-executor</li>
         * </ul>
         */
        private String executor = "worker-thread-executor";

        private int throughput = 1;

        private WorkerThreadExecutorConfig workerThreadExecutorConfig;

        public Builder() {
            // left empty so that jackson can create an
            // instance of this builder.
        }

        public Builder withExecutor(final String executor) {
            this.executor = executor;
            return this;
        }

        public Builder withThroughput(final int throughput) {
            this.throughput = throughput > 0 ? throughput : 1;
            return this;
        }

        public Builder withWorkerThreadExecutor(final WorkerThreadExecutorConfig config) {
            workerThreadExecutorConfig = config;
            return this;
        }

        public DispatcherConfiguration build() {
            if ("worker-thread-executor".equalsIgnoreCase(executor)) {


            } else {
                throw new IllegalArgumentException("Unknown executor \"" + executor + "\"");
            }
            return new DispatcherConfiguration(executor, throughput, workerThreadExecutorConfig);
        }

    }

}
