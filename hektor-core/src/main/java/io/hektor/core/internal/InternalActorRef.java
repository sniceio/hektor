package io.hektor.core.internal;

import io.hektor.core.ActorRef;
import io.hektor.core.Request;
import io.hektor.core.Response;

public interface InternalActorRef extends ActorRef {

    void dispatch(Object msg, ActorRef sender);

    void dispatch(Request request, ActorRef sender);

    void dispatch(Response response, ActorRef sender);
}
