package io.hektor.core.internal;

import com.codahale.metrics.MetricRegistry;
import io.hektor.core.Actor;
import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.hektor.core.RoutingLogic;
import io.hektor.core.Scheduler;
import io.hektor.core.internal.messages.Start;
import io.hektor.core.internal.messages.Watch;
import io.snice.protocol.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * The default implementation of Hektor.
 *
 * @author jonas@jonasborjesson.com
 */
public final class DefaultHektor implements InternalHektor {

    private InternalDispatcher defaultDispatcher;

    private final ActorPath root;

    private final MetricRegistry metricRegistry;

    private final ActorStore actorStore;

    private final Scheduler scheduler;

    public DefaultHektor(final ActorPath root, final Scheduler scheduler, final ActorStore actorStore, final MetricRegistry metricRegistry) {
        this.root = root;
        this.scheduler = scheduler;
        this.actorStore = actorStore;
        this.metricRegistry = metricRegistry;
    }

    public void setDefaultDispatcher(final InternalDispatcher dispatcher) {
        defaultDispatcher = dispatcher;
    }

    @Override
    public ActorRef actorOf(final String name, final Props props) {
        return actorOf(root, name, props);
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public ActorRef actorOf(final ActorPath parent, final String name, final Props props) {
        final ActorPath path = parent.createChild(name);
        final InternalDispatcher dispatcher = findDispatcher(path, props);
        final InternalActorRef ref = new DefaultActorRef(path, dispatcher);

        final ActorContext oldCtx = Actor._ctx.get();
        try {
            final ActorContext ctx = new ConstructorActorContext(this, ref);
            Actor._ctx.set(ctx);
            final Actor actor = ReflectionHelper.constructActor(props);
            dispatcher.register(ref, actor);
        } finally {
            Actor._ctx.set(oldCtx);
        }

        ref.dispatch(Start.MSG, ref);
        return ref;
    }

    @Override
    public Optional<ActorBox> lookupActorBox(final ActorRef ref) {
        return actorStore.lookup(ref);
    }

    @Override
    public Optional<ActorBox> lookupActorBox(final ActorPath path) {
        return actorStore.lookup(path);
    }

    @Override
    public Optional<ActorBox> lookupActorBox(final String path) throws  IllegalArgumentException {
        return actorStore.lookup(DefaultActorPath.create(root, path));
    }

    @Override
    public void removeActor(final ActorRef ref) {
        actorStore.remove(ref);
    }

    @Override
    public boolean watch(final ActorRef watcher, final ActorRef target) {
        final Optional<ActorBox> box = actorStore.lookup(target);
        if (box.isPresent()) {
            return false;
        }

        watcher.tell(Watch.MSG, target);
        return true;
    }

    @Override
    public Optional<ActorRef> lookup(final String path) throws IllegalArgumentException {
        return lookupActorBox(path).map(ActorBox::ref);
    }

    @Override
    public Optional<ActorRef> lookup(final ActorPath path) throws IllegalArgumentException {
        return lookupActorBox(path).map(ActorBox::ref);
    }

    @Override
    public RouterBuilder routerWithName(final String name) {
        return new Builder(root, name);
    }

    @Override
    public CompletionStage<Void> terminate() {
        return defaultDispatcher.shutdown();
    }

    /**
     * Find a suitable dispatcher for the actor identified by the path and its props.
     *
     * @param path
     * @param props
     * @return
     */
    private InternalDispatcher findDispatcher(final ActorPath path, final Props props) {
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
        public RouterBuilder withRoutingLogic(final RoutingLogic logic) {
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

        private RouterActorRef(final RoutingLogic routingLogic, final ActorPath path, final List<ActorRef> actors) {
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
            tell(Priority.NORMAL, msg, sender);
        }

        @Override
        public CompletionStage<Object> ask(final Object msg, final ActorRef sender) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public <T> Request<ActorRef, T> request(final ActorRef sender, final T msg) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public <T> Request<ActorRef, T> request(final Request<ActorRef, T> request) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public void tell(final Priority priority, final Object msg, final ActorRef sender) {
            final ActorRef receiver = routingLogic.select(msg, actors);
            receiver.tell(priority, msg, sender);

        }

        @Override
        public void tellAnonymously(final Object msg) {
            final ActorRef receiver = routingLogic.select(msg, actors);
            receiver.tellAnonymously(msg);
        }

        @Override
        public void monitor(final ActorRef ref) {
            throw new RuntimeException("Sorry, not implemented just yet");
        }

    }


}
