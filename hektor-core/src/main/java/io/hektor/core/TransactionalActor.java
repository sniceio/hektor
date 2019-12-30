package io.hektor.core;

import java.util.concurrent.CompletionStage;

public interface TransactionalActor extends Actor {

    /**
     * The {@link ActorRef#tell(Object)} method, which is the most common way to communicate with an {@link Actor},
     * is a fire-and-forget way of sending the other actor a message. If you expect an answer, you can use the
     * {@link ActorRef#ask(Object, ActorRef)} method and then add logic on the {@link CompletionStage} but what if you
     * want to have a more "traditional" request/response exchange between two actors, you cannot typically get that
     * unless you add such a protocol yourself to your own actors.
     *
     * {@link Actor}s are an excellent framework to build upon but if you are terminating a wire
     * protocol (HTTP, SIP, GTP etc) then you will have to send back a response at some point. The glue code between
     * the wire protocol and your actors probably could use the {@link ActorRef#ask(Object, ActorRef)} for certain
     * protocols that do not expect more than a single response back for each request, such as HTTP, but for SIP, this
     * would not be enough. In SIP, a single request can result in many provisional responses and then a single final
     * response. In order to implement that would have to re-invent the wheel and have a request/response type of
     * messages between your actors.
     *
     * So, if this is the situation you find yourself in, then use the
     *
     *
     * There are situations where you want to request information from another {@link Actor} and you
     * expect a response back at some point later and perhaps even with provisional responses. Either,
     * you have to build that into your own {@link Actor}s or you could use the {@link ActorRef#ask(Object, ActorRef)}
     * method, but in that case you cannot get provisional responses since the {@link CompletionStage} would need
     * to be completed on the first response.
     *
     *
     * @param request
     */
    default void onRequest(final Request request) {
        onReceive(request);
    }

    default void onResponse(final Response response) {
        onReceive(response);
    }

    @Override
    default boolean hasTransactionalSupport() {
        return true;
    }

    @Override
    default TransactionalActor toTransactionalActor() {
        return this;
    }
}
