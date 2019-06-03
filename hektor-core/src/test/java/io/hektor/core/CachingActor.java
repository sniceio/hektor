package io.hektor.core;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CachingActor implements Actor {

    public static final Props<CachingActor> props(final List<Object> cache, final CountDownLatch latch) {
        return Props.forActor(CachingActor.class, () -> new CachingActor(cache, latch));
    }

    private final List<Object> msgs;
    private final CountDownLatch latch;

    private CachingActor(final List<Object> cache, final CountDownLatch latch) {
        this.msgs = cache;
        this.latch = latch;
    }

    @Override
    public void onReceive(final Object msg) {
        synchronized (msgs) {
            msgs.add(msg);
        }
        latch.countDown();
    }
}
