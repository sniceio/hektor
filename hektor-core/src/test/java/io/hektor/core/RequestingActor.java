package io.hektor.core;

import io.snice.protocol.Request;
import io.snice.protocol.Response;
import io.snice.protocol.TransactionId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RequestingActor implements TransactionalActor {

    private final ActorRef respondingActor;
    private final CountDownLatch requestLatch;
    private final CountDownLatch responseLatch;

    private final Map<TransactionId, Request> requests = new HashMap<>();
    private final Map<TransactionId, List<Response>> responses = new HashMap<>();

    public static Props props(final CountDownLatch requestLatch, final CountDownLatch responseLatch, final ActorRef ref) {
        return Props.forActor(RequestingActor.class, () -> new RequestingActor(requestLatch, responseLatch, ref));
    }

    private RequestingActor(final CountDownLatch requestLatch, final CountDownLatch responseLatch, final ActorRef respondingActor) {
        this.requestLatch = requestLatch;
        this.respondingActor = respondingActor;
        this.responseLatch = responseLatch;
    }

    @Override
    public void onReceive(final Object msg) {
        final String str = (String)msg;
        switch (str) {
            case "responses":
                sender().tell(new HashMap(responses), self());
                break;
            case "requests":
                sender().tell(new HashMap(requests), self());
                break;
            default:
                final Request req = respondingActor.request(msg, self());
                requests.computeIfAbsent(req.getTransactionId(), transaction -> {
                    responses.put(transaction, new ArrayList<>());
                    return req;
                });
                requests.put(req.getTransactionId(), req);
                requestLatch.countDown();
        }
    }

    @Override
    public void onResponse(final Response response) {
        responses.get(response.getTransactionId()).add(response);
        responseLatch.countDown();
    }
}
