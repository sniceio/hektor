package io.hektor.actors.fsm;

import io.hektor.core.ActorContext;

public interface FsmActorContextAdaptor extends FsmActorContextSupport, ActorContext {

    void tellSubscribers(final Object msg);

}
