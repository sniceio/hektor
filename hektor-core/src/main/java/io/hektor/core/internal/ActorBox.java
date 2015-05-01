package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;

/**
 * @author jonas@jonasborjesson.com
 */
public class ActorBox {
    private final Actor actor;
    private final ActorRef ref;

    public ActorBox(final Actor actor, final ActorRef ref) {
        this.actor = actor;
        this.ref = ref;
    }

    public ActorRef ref() {
        return ref;
    }

    public Actor actor() {
        return actor;
    }
}
