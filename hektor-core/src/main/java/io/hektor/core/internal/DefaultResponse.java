package io.hektor.core.internal;

import io.hektor.core.ActorRef;
import io.snice.protocol.Response;
import io.snice.protocol.TransactionId;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultResponse implements Response<ActorRef> {

    private final Object msg;
    private final ActorRef owner;
    private final boolean isFinal;
    private final TransactionId id;

    private DefaultResponse(final TransactionId id, final ActorRef owner, final Object msg, final boolean isFinal) {
        this.id = id;
        this.owner = owner;
        this.msg = msg;
        this.isFinal = isFinal;
    }

    public static Builder of(final ActorRef owner, final TransactionId id) {
        assertNotNull(owner);
        assertNotNull(id);
        return new Builder(owner, id);
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    public Object getMessage() {
        return msg;
    }

    @Override
    public ActorRef getOwner() {
        return owner;
    }

    @Override
    public TransactionId getTransactionId() {
        return id;
    }

    public static class Builder implements Response.Builder<ActorRef> {

        private final TransactionId id;
        private final ActorRef owner;
        private boolean isFinal = true;
        private Object msg;

        private Builder(final ActorRef owner, final TransactionId id) {
            this.owner = owner;
            this.id = id;
        }

        @Override
        public Builder isFinal(final boolean value) {
            isFinal = value;
            return this;
        }

        public Builder withMessage(final Object msg) {
            this.msg = msg;
            return this;
        }

        @Override
        public DefaultResponse build() {
            return new DefaultResponse(id, owner, msg, isFinal);
        }
    }
}
