package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;

import java.util.HashMap;
import java.util.Map;

import static io.hektor.core.internal.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public class ActorBox {
    private final Actor actor;
    private final ActorRef ref;
    private final Map<String, ActorRef> children = new HashMap<>();

    private ActorBox(final Actor actor, final ActorRef ref) {
        this.actor = actor;
        this.ref = ref;
    }

    public static ActorBox create(final Actor actor, final ActorRef ref) {
        assertNotNull(actor);
        assertNotNull(ref);
        return new ActorBox(actor, ref);
    }



    public ActorRef getChild(final String name) {
        return children.get(name);
    }

    public ActorRef ref() {
        return ref;
    }

    public Actor actor() {
        return actor;
    }
}
