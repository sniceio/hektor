package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public interface ActorStore {

    ActorBox lookup(ActorRef ref);

    void store(ActorRef ref, Actor actor);

    ActorBox remove(ActorRef ref);
}
