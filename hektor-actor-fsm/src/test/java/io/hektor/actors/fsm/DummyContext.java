package io.hektor.actors.fsm;

import io.hektor.fsm.Context;

import java.util.concurrent.CountDownLatch;

public class DummyContext implements Context {

    private final CountDownLatch exitLatch = new CountDownLatch(1);

    public void doExit() {
        exitLatch.countDown();
    }

    public CountDownLatch exitLatch() {
        return exitLatch;
    }
}
