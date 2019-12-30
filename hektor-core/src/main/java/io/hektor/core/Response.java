package io.hektor.core;

public interface Response {

    boolean isFinal();

    Object getMessage();

    TransactionId getTransactionId();
}
