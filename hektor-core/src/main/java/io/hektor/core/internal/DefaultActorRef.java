package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Dispatcher;
import io.hektor.core.internal.messages.Watch;
import io.snice.protocol.Request;
import io.snice.protocol.Response;

import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.assertArgument;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultActorRef implements InternalActorRef {

    /**
     * The path to the actor.
     */
    private final ActorPath path;

    private final Dispatcher dispatcher;

    public DefaultActorRef(final ActorPath path, final Dispatcher dispatcher) {
        this.path = path;
        this.dispatcher = dispatcher;
    }

    @Override
    public ActorPath path() {
        return path;
    }

    @Override
    public void tell(final Object msg, final ActorRef sender) {
        processMessage(msg, sender, null, null);
    }

    @Override
    public CompletionStage<Object> ask(final Object msg, final ActorRef asker) {
        final ActorContext ctx = Actor._ctx.get();
        try {
            if (ctx != null) {
                return ((DefaultActorContext) ctx).ask(msg, this);
            }
        } catch (final ClassCastException e) {
            // odd, should have been a DefaultActorContext...
        }

        return dispatcher.ask(asker, this, msg);
    }

    private void processMessage(final Object msg, final ActorRef sender, final DefaultRequest request, final DefaultResponse response) {
        final ActorContext ctx = Actor._ctx.get();
        if (ctx != null) {
            try {
                final DefaultActorContext bufCtx = (DefaultActorContext) ctx;
                if (msg != null) {
                    bufCtx.buffer(msg, this, sender);
                } else if(request != null) {
                    bufCtx.buffer(this, sender, request);
                } else if(response != null) {
                    bufCtx.buffer(this, sender, response);
                } else {
                    // error
                    throw new Error("Hektor Internal Error - one of the msg, Request or Response must be set. " +
                            "Seems like a bug");
                }
            } catch (final ClassCastException e) {
                // ignore. shouldn't happen but if it does
                // then just dispatch the message.
                dispatcher.dispatch(sender, this, msg);
            }
        } else {
            // if the actor context is null, that means that this actor ref is
            // accessed outside of an actor "invocation" (meaning outside of e.g.
            // onReceive, the constructor etc etc) and as such, we'll just
            // hand it off to the dispatcher to take care of it.
            dispatcher.dispatch(sender, this, msg);
        }
    }


    @Override
    public void dispatch(final Object msg, final ActorRef sender) {
        dispatcher.dispatch(sender, this, msg);
    }

    @Override
    public void dispatch(final Request request, final ActorRef sender) {
        dispatcher.dispatch(sender, this, request);
    }

    @Override
    public void dispatch(final Response response, final ActorRef sender) {
        dispatcher.dispatch(sender, this, response);
    }

    @Override
    public Request<ActorRef> request(final Object msg, final ActorRef sender) {
        final DefaultRequest req = DefaultRequest.create(sender, msg);
        processMessage(null, sender, req, null);
        return req;
    }

    @Override
    public Response respond(final Object msg, final Request req, final ActorRef sender, final boolean isFinal) {
        final DefaultRequest request = (DefaultRequest)req;
        assertArgument(request.getOwner().equals(this), "You must send the response to the same actor that " +
                "initiated the original request. You tried to send the response to " +
                this + "but the actor who sent the request is " + request.getOwner());

        final DefaultResponse response = request.createResponse().withMessage(msg).isFinal(isFinal).build();
        processMessage(null, sender, null, response);
        return response;
    }

    @Override
    public void tell(final Priority priority, final Object msg, final ActorRef sender) {
        tell(msg, sender);
    }

    @Override
    public void tellAnonymously(final Object msg) {
        dispatcher.dispatch(ActorRef.None(), this, msg);
    }

    @Override
    public void monitor(final ActorRef ref) {
        dispatcher.dispatch(this, ref, Watch.MSG);
    }

    @Override
    public String toString()  {
        return path.toString();
    }
}
