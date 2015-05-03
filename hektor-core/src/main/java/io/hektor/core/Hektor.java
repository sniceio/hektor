package io.hektor.core;

import com.codahale.metrics.MetricRegistry;
import io.hektor.config.DispatcherConfiguration;
import io.hektor.config.HektorConfiguration;
import io.hektor.core.internal.ActorStore;
import io.hektor.core.internal.DefaultHektor;
import io.hektor.core.internal.InternalDispatcher;
import io.hektor.core.internal.SimpleActorStore;
import io.hektor.core.internal.workerexecutor.DefaultDispatcher;

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

    RouterBuilder routerWithName(String name);

    interface RouterBuilder {

        ActorRef build();

        RouterBuilder withRoutee(ActorRef actor);

        RouterBuilder withRoutingLogic(RoutingLogic logic);

    }

    class Builder {

        private final String name;

        private HektorConfiguration config;

        private MetricRegistry metricRegistry;

        private ActorStore actorStore;

        private Builder(final String name) {
            this.name = name;
        }

        public Builder withConfiguration(final HektorConfiguration config) {
            this.config = config;
            return this;
        }

        public Builder withActorStore(final ActorStore actorStore) {
            this.actorStore = actorStore;
            return this;
        }

        public Builder withMetricRegistry(final MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
            return this;
        }

        public Hektor build() {
            MetricRegistry registry = metricRegistry != null ? metricRegistry : new MetricRegistry();
            final Map<String, DispatcherConfiguration> dispatcherConfigs = config.dispatchers();
            if (actorStore == null) {
                actorStore = new SimpleActorStore();
            }
            final InternalDispatcher defaultDispatcher = createDefaultDispatcher(actorStore, dispatcherConfigs, registry);
            return new DefaultHektor(defaultDispatcher, registry);
        }

        /**
         * If no dispatcher configuration has been specified we will
         * generaet a default dispatcher.
         *
         * @return
         */
        private InternalDispatcher createDefaultDispatcher(final ActorStore actorStore,
                                                           final Map<String, DispatcherConfiguration> configs,
                                                           final MetricRegistry registry) {
            if (configs.size() == 1) {
                Map.Entry<String, DispatcherConfiguration> entry = configs.entrySet().iterator().next();
                return new DefaultDispatcher(entry.getKey(), actorStore, registry, entry.getValue());
            }

            throw new IllegalArgumentException("Missing configuration");
        }

    }
}
