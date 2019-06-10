package io.hektor.actors.io;

import io.hektor.actors.LoggingSupport;
import io.hektor.core.Actor;
import io.hektor.core.ActorContext;
import io.hektor.core.ActorRef;
import io.hektor.core.LifecycleEvent;
import io.hektor.core.Props;
import io.snice.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author jonas@jonasborjesson.com
 */
public class TokenReceiverActor implements LoggingSupport, Actor {

    private final Logger logger = LoggerFactory.getLogger(TokenReceiverActor.class);

    private final CountDownLatch latch;
    private final InputStream is;
    private final ExecutorService threadPool;
    private ActorRef myChild;

    private final List<Buffer> buffers = new ArrayList<>();

    public static Props props(final CountDownLatch latch, final InputStream is, final ExecutorService threadPool) {
        return Props.forActor(TokenReceiverActor.class, () -> new TokenReceiverActor(latch, is, threadPool));
    }

    public TokenReceiverActor(final CountDownLatch latch, final InputStream is, final ExecutorService threadPool) {
        this.latch = latch;
        this.is = is;
        this.threadPool = threadPool;
    }

    @Override
    public void start() {
        logInfo("Starting");
        myChild = ctx().actorOf("reader", InputStreamActor.props(is, threadPool));
    }

    @Override
    public void onReceive(final Object msg) {
        final ActorContext context = ctx();

        if (msg instanceof StreamToken) {
            final StreamToken token = (StreamToken) msg;
            buffers.add(token.getBuffer());
        } else if (msg instanceof LifecycleEvent.Terminated && ((LifecycleEvent.Terminated)msg).isActor(myChild)) {
            latch.countDown();
        } else if ("GiveMeResultPlease".equals(msg.toString()) ) {
            sender().tell(buffers.stream().map(Buffer::toString).collect(Collectors.joining()));
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Object getUUID() {
        return self();
    }
}
