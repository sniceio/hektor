package io.hektor.core;

import io.hektor.config.DispatcherConfiguration;
import io.hektor.config.HektorConfiguration;
import io.hektor.core.internal.workerexecutor.DefaultDispatcher;
import io.hektor.core.internal.DefaultHektor;
import io.hektor.core.internal.InternalDispatcher;

import java.util.Map;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Hektor {

    static Builder withName(final String name) {
        return new Builder(name);
    }

    /**
     * Create a top-level actor.
     *
     * @param props
     * @param name the name under which the Actor will be registered.
     * @return
     */
    ActorRef actorOf(Props props, String name);

    class Builder {

        private final String name;

        private HektorConfiguration config;

        private Builder(final String name) {
            this.name = name;
        }

        public Builder withConfiguration(final HektorConfiguration config) {
            this.config = config;
            return this;
        }

        public Hektor build() {
            final Map<String, DispatcherConfiguration> dispatcherConfigs = config.dispatchers();
            final InternalDispatcher defaultDispatcher = createDefaultDispatcher(dispatcherConfigs);
            return new DefaultHektor(defaultDispatcher);
        }

        /**
         * If no dispatcher configuration has been specified we will
         * generaet a default dispatcher.
         *
         * @return
         */
        private InternalDispatcher createDefaultDispatcher(final Map<String, DispatcherConfiguration> configs) {
            if (configs.size() == 1) {
                Map.Entry<String, DispatcherConfiguration> entry = configs.entrySet().iterator().next();
                return new DefaultDispatcher(entry.getKey(), entry.getValue());
            }

            throw new IllegalArgumentException("Missing configuration");
        }

    }
}
