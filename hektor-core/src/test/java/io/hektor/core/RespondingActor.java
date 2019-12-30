package io.hektor.core;

import io.hektor.core.internal.DefaultRequest;
import io.snice.protocol.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class RespondingActor implements TransactionalActor {

    public static Props props() {
        return Props.forActor(RespondingActor.class, () -> new RespondingActor());
    }

    private final Map<String, DefaultRequest> requests = new HashMap<>();

    private RespondingActor() {
        // left empty intentionally
    }

    @Override
    public void onReceive(final Object msg) {
        final String cmd = (String)msg;

        switch (getCommand(cmd)) {
            case "provisional":
                processTransaction(cmd, false);
                break;
            case "final":
                processTransaction(cmd, true);
                break;
            default:
                throw new RuntimeException("Unknown command - you're probably missing something in the switch-statement");
        }
    }

    private String getCommand(final String msg) {
        return msg.split(" ")[0];
    }

    @Override
    public void onRequest(final Request req) {
        final DefaultRequest request = (DefaultRequest)req;

        requests.put(request.getTransactionId().toString(), request);

        final String cmd = (String)request.getMessage();
        switch (cmd.split(" ")[0]) {
            case "hello":
                sender().respond("world", request, self());
                break;
            case "count":
                sender().respond(count(cmd), request, self());
                break;
            case "start":
                // just consume this one. We've saved the request (transaction)
                // and now is someone asks us to send a "provisional" or a "final"
                // response we'll do that then...
                break;
            default:
                sender().respond("unknown command", request, self());
        }
    }

    /**
     * The "command" format is expected to be:
     *
     * <code>
     * <"provisional" | "final"> SP <transaction_id> SP <message>
     * </code>
     *
     * @param cmd
     * @return
     */
    private void processTransaction(final String cmd, final boolean isFinal) {
        final DefaultRequest request = ensureRequest(cmd.split(" ")[1]);
        final String msg = cmd.split(" ", 3)[2];
        request.getOwner().respond(msg, request, self(), isFinal);
    }

    private DefaultRequest ensureRequest(final String transactionId) {
        final DefaultRequest req = requests.get(transactionId);
        assertNotNull(req);
        return req;
    }

    private static String count(final String count) {
        return IntStream.range(0, Integer.parseInt(count.split(" ")[1])).mapToObj(Integer::toString).collect(Collectors.joining(" "));
    }

}
