package io.hektor.core;

import java.util.concurrent.CountDownLatch;

public class SelfishActor implements Actor {

    private int count;
    private int max;
    private final CountDownLatch latch;

    public static Props props(final CountDownLatch latch) {
        return Props.forActor(SelfishActor.class, () -> new SelfishActor(latch));
    }


    public SelfishActor(final CountDownLatch latch) {
        this.latch = latch;
        this.max = (int)latch.getCount();
    }

    @Override
    public void onReceive(Object msg) {
        System.err.println("Received " + msg);
        latch.countDown();

        if (++count == max) {
            System.err.println("I'm done!");
        } else {
            System.err.println("Selflishly sending to myself!");
            self().tellAnonymously(msg.toString() + count);
        }
    }
}
