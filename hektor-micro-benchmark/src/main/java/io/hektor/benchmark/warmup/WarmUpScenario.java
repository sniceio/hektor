package io.hektor.benchmark.warmup;

import io.hektor.benchmark.warmup.messages.Messages.SimpleMessage;
import io.hektor.benchmark.warmup.messages.Messages.TerminateYourself;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.hektor.core.Props;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Simple warm-up scenario.
 *
 * @author jonas@jonasborjesson.com
 */
public class WarmUpScenario {

    private Hektor hektor;

    public WarmUpScenario(final Hektor hektor) {
        this.hektor = hektor;
    }

    /**
     * Run the warmup scenario.
     */
    public void warmUp(final int actorCount) throws Exception {
        final CountDownLatch postStopLatch = new CountDownLatch(actorCount);
        final String name = "foo";
        final ActorRef[] actors = new ActorRef[actorCount];
        for (int i = 0; i < actorCount; ++i) {
            final Props props = WarmUpActor.props(postStopLatch);
            final ActorRef actor = hektor.actorOf(props, name + i);
            actors[i] = actor;
        }

        // make sure that they are all there.
        for (int i = 0; i < actorCount; ++i) {
            hektor.lookup(name + i).orElseThrow(RuntimeException::new);
        }

        // send a simple message to them all
        for (int i = 0; i < actorCount; ++i) {
            actors[i].tellAnonymously(new SimpleMessage("hello"));
        }

        // ask them all to kill themselves
        for (int i = 0; i < actorCount; ++i) {
            actors[i].tellAnonymously(new TerminateYourself());
        }

        postStopLatch.await(100, TimeUnit.MILLISECONDS);

        // Thread.sleep(1000);

        for (int i = 0; i < actorCount; ++i) {
            hektor.lookup(name + i).ifPresent(ref -> System.err.println("Error"));
        }
    }

}
