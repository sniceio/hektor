package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.hektor.core.Props;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The default implementation of Hektor.
 *
 * @author jonas@jonasborjesson.com
 */
public class DefaultHektor implements Hektor {

    private final InternalDispatcher defaultDispatcher;

    private final ActorPath root = new DefaultActorPath(null, "hektor");

    public DefaultHektor(InternalDispatcher defaultDispatcher) {
        this.defaultDispatcher = defaultDispatcher;
    }

    @Override
    public ActorRef actorOf(final Props props, final String name) {
        final Class<? extends Actor> clazz = props.clazz();
        try {
            Constructor<? extends Actor> constructor =  clazz.getConstructor();
            final Actor actor = constructor.newInstance();
            final ActorPath path = root.createChild(name);
            final ActorRef ref = new DefaultActorRef(path, defaultDispatcher);
            ActorBox actorBox = new ActorBox(actor, ref);
            final InternalDispatcher dispatcher = findDispatcher(props);
            dispatcher.register(actorBox);
            return ref;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InternalDispatcher findDispatcher(final Props props) {
        return defaultDispatcher;
    }

}
