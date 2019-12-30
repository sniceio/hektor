package io.hektor.core.internal;

import io.hektor.core.ActorRef;
import io.snice.protocol.Request;
import io.snice.protocol.TransactionId;

import java.util.Objects;

import static io.hektor.core.internal.PreConditions.assertNotNull;

public class DefaultRequest implements Request<ActorRef> {

    private final Object msg;
    private final ActorRef sender;
    private final TransactionId transactionId;

    public static DefaultRequest create(final ActorRef sender, final Object msg) {
        assertNotNull(sender);
        assertNotNull(msg);
        return new DefaultRequest(sender, msg, TransactionId.generateDefault());
    }

    private DefaultRequest(final ActorRef sender, final Object msg, final TransactionId transactionId) {
        this.sender = sender;
        this.msg = msg;
        this.transactionId = transactionId;
    }

    public Object getMessage() {
        return msg;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DefaultRequest that = (DefaultRequest) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return transactionId.hashCode();
    }

    @Override
    public DefaultResponse.Builder createResponse() {
        return DefaultResponse.of(sender, transactionId);
    }

    @Override
    public ActorRef getOwner() {
        return sender;
    }

    @Override
    public TransactionId getTransactionId() {
        return transactionId;
    }
}
