package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple actor store backed by a concurrent hash map.
 *
 * @author jonas@jonasborjesson.com
 */
public class SimpleActorStore implements ActorStore {

    private final Map<ActorPath, ActorBox> actors = new ConcurrentHashMap<>(100);

    @Override
    public Optional<ActorBox> lookup(final ActorRef ref) {
        return lookup(ref.path());
    }

    @Override
    public Optional<ActorBox> lookup(final ActorPath path) {
        return Optional.ofNullable(actors.get(path));
    }

    @Override
    public void store(final ActorRef ref, final Actor actor) {
        actors.put(ref.path(), ActorBox.create(new DefaultMailBox(), actor, ref));
    }

    @Override
    public Optional<ActorBox> remove(final ActorRef ref) {
        return Optional.ofNullable(actors.remove(ref.path()));
    }

}
