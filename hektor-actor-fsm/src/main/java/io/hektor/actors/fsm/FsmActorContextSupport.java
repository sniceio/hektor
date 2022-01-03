package io.hektor.actors.fsm;

import io.hektor.core.ActorContext;
import io.hektor.fsm.Context;

/**
 * When executing the FSM within the Hektor Actor environment, your implementing class of {@link Context}
 * can optionally implement this interface, which then provides it with a way to get hold of the
 * {@link ActorContext} etc.
 */
public interface FsmActorContextSupport {

    ThreadLocal<FsmActorContextAdaptor> _ctx = new ThreadLocal<>();

    default void tellSubscribers(final Object msg) {
        _ctx.get().tellSubscribers(msg);
    }

    default ActorContext ctx() {
        return _ctx.get();
    }
}
