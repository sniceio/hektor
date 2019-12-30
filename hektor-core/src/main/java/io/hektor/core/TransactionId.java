package io.hektor.core;

import io.hektor.core.internal.UuidTransactionId;

public interface TransactionId {
    static TransactionId generate() {
        return UuidTransactionId.generate();
    }
}
