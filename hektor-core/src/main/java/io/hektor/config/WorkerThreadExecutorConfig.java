package io.hektor.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jonas@jonasborjesson.com
 */
@JsonDeserialize(builder = WorkerThreadExecutorConfig.Builder.class)
public class WorkerThreadExecutorConfig {

    private final int noOfWorkers;
    private final ExecutorService executorService;

    private WorkerThreadExecutorConfig(final int workers, final ExecutorService executor) {
        noOfWorkers = workers;
        executorService = executor;
    }

    public int getNoOfWorkers() {
        return noOfWorkers;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public static class Builder {

        private int noOfWorkers;

        public Builder() {
            // left empty intentionally.
        }

        public Builder withNoOfWorkers(final int workers) {
            noOfWorkers = workers;
            return this;
        }

        public WorkerThreadExecutorConfig build() {
            final ExecutorService service = Executors.newFixedThreadPool(noOfWorkers);
            return new WorkerThreadExecutorConfig(noOfWorkers, service);
        }

    }
}
