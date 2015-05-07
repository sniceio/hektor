package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jonas@jonasborjesson.com
 */
public class ActorBox {
    private final Actor actor;
    private final ActorRef ref;

    /**
     * A map of all the children that has belongs to this actor.
     * Remember, it is guaranteed that the ActorBox is only
     * accessed in a "thread safe" manner so no need to
     * have any additional locks/concurrent maps or whatever.
     *
     * The current execution context MUST guarantee this.
     */
    private final Map<String, ActorRef> children = new HashMap<>();

    private ActorBox(final Actor actor, final ActorRef ref) {
        this.actor = actor;
        this.ref = ref;
    }

    public static ActorBox create(final Actor actor, final ActorRef ref) {
        return new ActorBox(actor, ref);
    }

    public ActorRef getChild(final String name) {
        return children.get(name);
    }

    public boolean hasChild(final String name) {
        return children.containsKey(name);
    }

    public ActorRef ref() {
        return ref;
    }

    public Actor actor() {
        return actor;
    }
}
