package io.hektor.core;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertCollectionNotEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Testing the request/response mechanism of Hektor.
 */
public class RequestResponseTest extends HektorTestBase {

    private ActorRef responder;
    private ActorRef requester;

    private CountDownLatch requestLatchCount1;

    private CountDownLatch responseLatchCount1;
    private CountDownLatch responseLatchCount2;
    private CountDownLatch responseLatchCount3;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        requestLatchCount1 = new CountDownLatch(1);

        responseLatchCount1 = new CountDownLatch(1);
        responseLatchCount2 = new CountDownLatch(2);
        responseLatchCount3 = new CountDownLatch(2);

        responder = defaultHektor.actorOf(RespondingActor.props(), "Responder");
        requester = defaultHektor.actorOf(RequestingActor.props(requestLatchCount1, responseLatchCount1, responder), "Requester");
    }


    /**
     * A simple basic request/response scenario, without any complications.
     */
    @Test(timeout = 2000L)
    public void testBasicRequestResponse() throws Exception {
        requester.tell("hello");
        responseLatchCount1.await();

        final Request request = getRequest(requester);
        assertThat(request, notNullValue());
        assertThat(request.getMessage(), is("hello"));

        final Map<TransactionId, List<Response>> responses = getResponses(requester);
        assertThat(responses.size(), CoreMatchers.is(1));

        final Response response = responses.values().iterator().next().get(0);
        assertThat(response.isFinal(), is(true));
        assertThat(response.getMessage(), is("world"));
        assertThat(response.getTransactionId(), is(request.getTransactionId()));
    }

    /**
     * Slightly more complicated where we have two transactions outstanding at the same time.
     *
     * @throws Exception
     */
    @Test(timeout = 2000L)
    public void testRequestResponseTwoOutstandingTransactions() throws Exception {
        requester = defaultHektor.actorOf(RequestingActor.props(requestLatchCount1, responseLatchCount2, responder), "Requester2");
        requester.tell("hello");
        requester.tell("count 6");
        responseLatchCount2.await();

        final Map<TransactionId, Request> requests = getRequests(requester);
        final Map<TransactionId, List<Response>> responses = getResponses(requester);
        assertThat(requests.size(), is(2));
        assertThat(responses.size(), is(2));

        final Request helloRequest = findRequest(requests, "hello");
        final Response helloResponse = ensureSingleResponse(requester, helloRequest.getTransactionId());
        assertResponse(helloResponse, "world");

        final Request countRequest = findRequest(requests, "count 6");
        final Response countResponse = ensureSingleResponse(requester, countRequest.getTransactionId());
        assertResponse(countResponse, "0 1 2 3 4 5");
    }

    /**
     * Make sure that we also can do provisional responses.
     *
     * The way this test works is that we kick things off by telling the {@link RequestingActor}
     * to start with a new Request with the "magic" command "provisional", which the {@link RespondingActor}
     * will parse, read and understand and will then issue a provisional response instead of a final.
     *
     * Then, we will tell the {@link RespondingActor} to keep issue provisional responses for the given
     * transaction until we go "final".
     */
    // @Test(timeout = 2000L)
    @Test
    public void testProvisionalResponses() throws Exception {
        requester = defaultHektor.actorOf(RequestingActor.props(requestLatchCount1, responseLatchCount3, responder), "Requester2");
        requester.tell("start");

        // we need the transaction id so wait until the requestLatch opens and then fetch that Request object.
        // Then send commands to the RespondingActor with the transaction id etc.
        final Request request = getRequest(requester);
        responder.tell(provisionalResponse(request, "one"));
        responder.tell(provisionalResponse(request, "two"));
        responder.tell(finalResponse(request, "three"));

        responseLatchCount3.await();
        final List<Response> responses = getResponses(requester, request.getTransactionId());
        assertThat(responses.size(), is(3));
        assertResponse(responses.get(0), "one", false);
        assertResponse(responses.get(1), "two", false);
        assertResponse(responses.get(2), "three", true);
    }

    private String provisionalResponse(final Request request, final String msg) {
        return "provisional " + request.getTransactionId() + " " + msg;
    }

    private String finalResponse(final Request request, final String msg) {
        return "final " + request.getTransactionId() + " " + msg;
    }


    private List<Response> getResponses(final ActorRef ref, final TransactionId transactionId) throws Exception {
        final List<Response> list = getResponses(ref).get(transactionId);
        assertCollectionNotEmpty(list, "Expected at least one response for transaction " + transactionId);
        return list;
    }

    /**
     * Convenience method for making sure that there is only a single {@link Response} for the given
     * {@link TransactionId}.
     */
    private Response ensureSingleResponse(final ActorRef ref, final TransactionId transactionId) throws Exception {
        final List<Response> list = getResponses(ref, transactionId);
        assertArgument(list.size() == 1, "Only expected a single response for transaction " + transactionId);
        return list.get(0);
    }

    private Request findRequest(final Map<TransactionId, Request> requests, final String msg) {
        return requests.values().stream().filter(req -> msg.equals(req.getMessage()))
                .findFirst().orElseThrow(() -> new RuntimeException("Expected to find a Request containing message "
                        + msg + " but didn't"));
    }

    private void assertResponse(final Response response, final String expectedMsg) {
        assertResponse(response, expectedMsg, true);
    }

    private void assertResponse(final Response response, final String expectedMsg, final boolean isFinal) {
        assertThat(response.getMessage(), is(expectedMsg));
        assertThat(response.isFinal(), is(isFinal));
    }

    private Map<TransactionId, List<Response>> getResponses(final ActorRef actor) throws Exception {
        return (Map<TransactionId, List<Response>>)actor.ask("responses").toCompletableFuture().get();
    }

    private Map<TransactionId, Request> getRequests(final ActorRef actor) throws Exception {
        return (Map<TransactionId, Request>)actor.ask("requests").toCompletableFuture().get();
    }

    private Request getRequest(final ActorRef actor) throws Exception {
        final Map<TransactionId, Request> requests = (Map<TransactionId, Request>)actor.ask("requests").toCompletableFuture().get();
        assertThat(requests, notNullValue());
        return requests.values().iterator().next();
    }

}