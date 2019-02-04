package io.hektor.benchmark.warmup;

import io.hektor.benchmark.warmup.messages.Messages.CreateChild;
import io.hektor.benchmark.warmup.messages.Messages.TerminateYourself;
import io.hektor.core.Actor;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;

import java.util.concurrent.CountDownLatch;

/**
 * @author jonas@jonasborjesson.com
 */
public class WarmUpActor implements Actor {

    private ActorRef me;

    private final CountDownLatch postStopLatch;

    public static Props props(final CountDownLatch latch) {
        return Props.forActor(WarmUpActor.class, () -> new WarmUpActor(latch));
    }

    public WarmUpActor(final CountDownLatch postStopLatch) {
        // because we can
        me = ctx().self();
        this.postStopLatch = postStopLatch;
    }


    @Override
    public void postStop() {
        postStopLatch.countDown();
    }

    @Override
    public void onReceive(final Object msg) {
        if (msg instanceof TerminateYourself) {
            ctx().stop();
        } else if (msg instanceof CreateChild) {
            final String name = ((CreateChild) msg).child;
            ctx().actorOf(name, props(postStopLatch));
        }
    }
}
