package io.hektor.core;

import com.codahale.metrics.MetricRegistry;
import io.hektor.config.DispatcherConfiguration;
import io.hektor.config.HektorConfiguration;
import io.hektor.core.internal.ActorStore;
import io.hektor.core.internal.DefaultActorPath;
import io.hektor.core.internal.DefaultHektor;
import io.hektor.core.internal.HashWheelScheduler;
import io.hektor.core.internal.InternalDispatcher;
import io.hektor.core.internal.InternalHektor;
import io.hektor.core.internal.SimpleActorStore;
import io.hektor.core.internal.workerexecutor.DefaultDispatcher;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

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

    /**
     * Obtain the system scheduler that can be used to schedule messages
     * to be sent at some future point.
     *
     * @return
     */
    Scheduler scheduler();

    /**
     * Lookup the reference of an actor based on its path
     *
     * @param path
     * @return
     */
    Optional<ActorRef> lookup(String path);

    Optional<ActorRef> lookup(ActorPath path);

    RouterBuilder routerWithName(String name);

    CompletionStage<Void> terminate();

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

        private Scheduler scheduler;

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

        public Builder withScheduler(final Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public Builder withMetricRegistry(final MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
            return this;
        }

        public Hektor build() {
            final HektorConfiguration config = ensureConfiguration();
            MetricRegistry registry = metricRegistry != null ? metricRegistry : new MetricRegistry();
            final Map<String, DispatcherConfiguration> dispatcherConfigs = config.dispatchers();
            if (actorStore == null) {
                actorStore = new SimpleActorStore();
            }
            final ActorPath root = new DefaultActorPath(null, name);
            // TODO: the dispatcher doesn't need to know the root
            // it really should only be used for scheduling actors to run
            // and nothing else. No lookup an actor etc.
            final Scheduler scheduler = this.scheduler != null ? this.scheduler : new HashWheelScheduler();
            final DefaultHektor hektor = new DefaultHektor(root, scheduler, actorStore, registry);
            final InternalDispatcher defaultDispatcher = createDefaultDispatcher(root, hektor, actorStore, dispatcherConfigs, registry);
            hektor.setDefaultDispatcher(defaultDispatcher);
            return hektor;
        }

        private HektorConfiguration ensureConfiguration() {
            if (config != null) {
                return config;
            }

            return new HektorConfiguration();
        }

        /**
         * If no dispatcher configuration has been specified we will
         * generate a default dispatcher.
         *
         * @return
         */
        private InternalDispatcher createDefaultDispatcher(final ActorPath rootPath,
                                                           final InternalHektor hektor,
                                                           final ActorStore actorStore,
                                                           final Map<String, DispatcherConfiguration> configs,
                                                           final MetricRegistry registry) {
            if (configs.size() == 1) {
                final Map.Entry<String, DispatcherConfiguration> entry = configs.entrySet().iterator().next();
                return new DefaultDispatcher(entry.getKey(), rootPath, hektor, actorStore, registry, entry.getValue());
            }

            throw new IllegalArgumentException("Missing configuration");
        }

    }
}
