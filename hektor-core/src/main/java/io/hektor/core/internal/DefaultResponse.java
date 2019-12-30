package io.hektor.core.internal;

import io.hektor.core.Response;
import io.hektor.core.TransactionId;

import java.util.Objects;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultResponse implements Response {

    private final Object msg;
    private final boolean isFinal;
    private final DefaultRequest request;

    public static DefaultResponse create(final Object msg, final DefaultRequest request, final boolean isFinal) {
        assertNotNull(msg);
        assertNotNull(request);
        return new DefaultResponse(msg, request, isFinal);
    }

    private DefaultResponse(final Object msg, final DefaultRequest request, final boolean isFinal) {
        this.msg = msg;
        this.request = request;
        this.isFinal = isFinal;
    }

    public TransactionId getTransactionId() {
        return request.getTransactionId();
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public Object getMessage() {
        return msg;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DefaultResponse that = (DefaultResponse) o;
        return Objects.equals(request.getTransactionId(), that.request.getTransactionId());
    }

    @Override
    public int hashCode() {
        return request.getTransactionId().hashCode();
    }
}
