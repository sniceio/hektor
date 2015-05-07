package io.hektor.core.internal;

import com.codahale.metrics.MetricRegistry;
import io.hektor.core.Actor;
import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.hektor.core.Props;
import io.hektor.core.RoutingLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        final ActorPath path = root.createChild(name);
        final InternalDispatcher dispatcher = findDispatcher(props);
        final ActorRef ref = new DefaultActorRef(path, dispatcher);

        final ActorContext oldCtx = Actor._ctx.get();
        try {
            ActorContext ctx = new ConstructorActorContext(ref);
            Actor._ctx.set(ctx);
            final Actor actor = ReflectionHelper.constructActor(props);
            dispatcher.register(ref, actor);
        } finally {
            Actor._ctx.set(oldCtx);
        }

        return ref;
    }

    @Override
    public Optional<ActorRef> lookup(final String path) throws IllegalArgumentException {
        final Optional<ActorBox> box = defaultDispatcher.lookup(path);
        if (box.isPresent()) {
            return Optional.of(box.get().ref());
        }

        return Optional.empty();
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
