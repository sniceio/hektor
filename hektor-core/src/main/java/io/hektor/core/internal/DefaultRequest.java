package io.hektor.core.internal;

import io.hektor.core.ActorRef;
import io.hektor.core.Request;
import io.hektor.core.TransactionId;

import java.util.Objects;

import static io.hektor.core.internal.PreConditions.assertNotNull;

public class DefaultRequest implements Request {

    private final Object msg;
    private final ActorRef sender;
    private final TransactionId transactionId;

    public static DefaultRequest create(final ActorRef sender, final Object msg) {
        assertNotNull(sender);
        assertNotNull(msg);
        return new DefaultRequest(sender, msg, TransactionId.generate());
    }

    private DefaultRequest(final ActorRef sender, final Object msg, final TransactionId transactionId) {
        this.sender = sender;
        this.msg = msg;
        this.transactionId = transactionId;
    }

    @Override
    public TransactionId getTransactionId() {
        return transactionId;
    }

    @Override
    public ActorRef getSender() {
        return sender;
    }

    @Override
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
}
