package io.hektor.actors.fsm;

import io.hektor.core.ActorContext;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;

@FunctionalInterface
public interface OnStartFunction<C extends Context, D extends Data> {
    void start(ActorContext actorCtx, C ctx, D data);
}
