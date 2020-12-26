package io.hektor.actors.fsm;

import io.hektor.core.ActorContext;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;

@FunctionalInterface
public interface OnStopFunction<C extends Context, D extends Data> {
    void stop(ActorContext actorCtx, C ctx, D data);
}
