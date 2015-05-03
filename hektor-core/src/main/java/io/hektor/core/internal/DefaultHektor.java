package io.hektor.core.internal;

import com.codahale.metrics.MetricRegistry;
import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.hektor.core.Props;
import io.hektor.core.RoutingLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of Hektor.
 *
 * @author jonas@jonasborjesson.com
 */
public class DefaultHektor implements Hektor {

    private final InternalDispatcher defaultDispatcher;

    private final ActorPath root = new DefaultActorPath(null, "hektor");

    private final MetricRegistry metricRegistry;

    public DefaultHektor(final InternalDispatcher defaultDispatcher, final MetricRegistry metricRegistry) {
        this.defaultDispatcher = defaultDispatcher;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public ActorRef actorOf(final Props props, final String name) {
        final Actor actor = ReflectionHelper.constructActor(props);
        final ActorPath path = root.createChild(name);
        final ActorRef ref = new DefaultActorRef(path, defaultDispatcher);
        final InternalDispatcher dispatcher = findDispatcher(props);
        dispatcher.register(ref, actor);
        return ref;
    }

    @Override
    public RouterBuilder routerWithName(final String name) {
        return new Builder(root, name);
    }

    private InternalDispatcher findDispatcher(final Props props) {
        return defaultDispatcher;
    }

    static class Builder implements RouterBuilder {

        private String name;

        private final ActorPath path;

        private final List<ActorRef> routees = new ArrayList<>();

        private RoutingLogic routingLogic;

        private Builder(final ActorPath rootPath, final String name) {
            this.path = rootPath.createChild(name);
        }

        @Override
        public RouterBuilder withRoutee(final ActorRef actor) {
            routees.add(actor);
            return this;
        }

        @Override
        public RouterBuilder withRoutingLogic(RoutingLogic logic) {
            routingLogic = logic;
            return this;
        }

        @Override
        public ActorRef build() {
            if (routees.isEmpty()) {
                throw new IllegalArgumentException("A router needs routees");
            }

            if (routingLogic == null) {
                throw new IllegalArgumentException("You must specify the routing logic");
            }

            return new RouterActorRef(routingLogic, path, routees);
        }
    }

    static class RouterActorRef implements ActorRef {

        private final RoutingLogic routingLogic;
        private final ActorPath path;
        private final List<ActorRef> actors;

        private RouterActorRef(final RoutingLogic routingLogic, final ActorPath path, List<ActorRef> actors) {
            this.routingLogic = routingLogic;
            this.path = path;
            this.actors = actors;
        }

        @Override
        public ActorPath path() {
            return path;
        }

        @Override
        public void tell(final Object msg, final ActorRef sender) {
            final ActorRef receiver = routingLogic.select(msg, actors);
            receiver.tell(msg, sender);
        }

        @Override
        public void tellAnonymously(final Object msg) {
            final ActorRef receiver = routingLogic.select(msg, actors);
            receiver.tellAnonymously(msg);
        }

    }


}
