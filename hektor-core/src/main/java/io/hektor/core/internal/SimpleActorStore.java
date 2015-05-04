package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple actor store backed by a concurrent hash map.
 *
 * @author jonas@jonasborjesson.com
 */
public class SimpleActorStore implements ActorStore {

    private final Map<ActorPath, ActorBox> actors = new ConcurrentHashMap<>(100);

    @Override
    public ActorBox lookup(ActorRef ref) {
        return actors.get(ref.path());
    }

    @Override
    public void store(final ActorRef ref, final Actor actor) {
        actors.put(ref.path(), ActorBox.create(actor, ref));
    }

    @Override
    public ActorBox remove(final ActorRef ref) {
        return actors.remove(ref.path());
    }
}
