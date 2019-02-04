package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public interface ActorStore {

    Optional<ActorBox> lookup(ActorRef ref);

    Optional<ActorBox> lookup(ActorPath path);

    void store(ActorRef ref, Actor actor);

    Optional<ActorBox> remove(ActorRef ref);
}
