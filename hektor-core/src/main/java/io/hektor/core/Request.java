package io.hektor.core;

public interface Request {

    ActorRef getSender();

    Object getMessage();

    TransactionId getTransactionId();
}
