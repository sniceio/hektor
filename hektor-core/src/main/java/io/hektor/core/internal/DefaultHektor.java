package io.hektor.core.internal;

import com.codahale.metrics.MetricRegistry;
import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.hektor.core.Props;

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

    private InternalDispatcher findDispatcher(final Props props) {
        return defaultDispatcher;
    }

}
