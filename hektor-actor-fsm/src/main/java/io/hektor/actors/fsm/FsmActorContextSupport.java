package io.hektor.actors.fsm;

import io.hektor.core.ActorContext;
import io.hektor.core.ActorRef;
import io.hektor.fsm.Context;

/**
 * When executing the FSM within the Hektor Actor environment, your implementing class of {@link Context}
 * can optionally implement this interface, which then provides it with a way to get hold of the
 * {@link ActorContext} etc.
 */
public interface FsmActorContextSupport {

    ThreadLocal<ActorContext> _ctx = new ThreadLocal<>();

    default ActorRef self() {
        return _ctx.get().self();
    }

    default ActorRef sender() {
        return _ctx.get().sender();
    }

    default ActorContext ctx() {
        return _ctx.get();
    }
}
