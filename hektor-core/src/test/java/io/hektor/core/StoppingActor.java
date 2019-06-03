package io.hektor.core;

import io.hektor.core.internal.messages.Stop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class StoppingActor implements Actor {

    private final List<StopMessage> stop = new ArrayList<>();
    private final List<StopMessage> postStop = new ArrayList<>();

    public static Props<StoppingActor> props() {
        return Props.forActor(StoppingActor.class, () -> new StoppingActor());
    }

    @Override
    public void stop() {
        stop.stream().forEach(msg -> msg.receiver.tell(msg.msg, self()));
    }

    @Override
    public void postStop() {
        postStop.stream().forEach(msg -> msg.receiver.tell(msg.msg, self()));
    }

    @Override
    public void onReceive(final Object msg) {

        if (msg instanceof ParentActor.StopYourselfMessage) {
            ctx().stop();
        } else if (msg instanceof StopMessage) {
            final StopMessage s = (StopMessage)msg;
            if (s.sendAtStop) {
                stop.add(s);
            } else {
                postStop.add(s);
            }
        }
    }

    public static class StopMessage {
        private final Object msg;
        private final ActorRef receiver;
        private final boolean sendAtStop;

        public static final StopMessage stop(final ActorRef receiver, final Object msg) {
            return new StopMessage(true, receiver, msg);
        }

        public static final StopMessage postStop(final ActorRef receiver, final Object msg) {
            return new StopMessage(false, receiver, msg);
        }

        private StopMessage(final boolean sendAtStop, final ActorRef receiver, final Object msg) {
            this.sendAtStop = sendAtStop;
            this.msg = msg;
            this.receiver = receiver;
        }
    }
}
