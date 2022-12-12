package io.hektor.actors.io;

import io.hektor.actors.SubscriptionManagementSupport;
import io.hektor.core.Actor;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.snice.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * An {@link Actor} that operates with the terminal window and uses the
 * {@link InputStreamActor} and {@link OutputStreamActor}
 * to read & write to/from the console. You can specify another {@link InputStream} and {@link OutputStream}
 * to use but by default <code>System.in</code> and <code>System.out</code> will be used.
 */
public class ConsoleActor extends SubscriptionManagementSupport implements Actor {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleActor.class);

    public static Props props() {
        return props(System.in, System.out);

    }

    public static Props props(final InputStream in, final OutputStream out) {
        return props(in, out, Executors.newFixedThreadPool(2));
    }

    public static Props props(final InputStream in, final OutputStream out, final ExecutorService blockingIoPool) {
        assertNotNull(in, "You must specify the input stream");
        assertNotNull(out, "You must specify the output stream");
        assertNotNull(blockingIoPool, "You must specify the thread pool used for blocking IO operations");
        final ConsoleConfig consoleConfig = ConsoleConfig.of().build();
        return Props.forActor(ConsoleActor.class, () -> new ConsoleActor(consoleConfig, blockingIoPool, in, out));
    }

    public static Props props(final InputStream in,
                              final OutputStream out,
                              final ExecutorService blockingIoPool,
                              final ConsoleConfig config) {
        assertNotNull(in, "You must specify the input stream");
        assertNotNull(out, "You must specify the output stream");
        assertNotNull(blockingIoPool, "You must specify the thread pool used for blocking IO operations");
        assertNotNull(config, "If you decide to specify the Console Configuration then it cannot be null");
        return Props.forActor(ConsoleActor.class, () -> new ConsoleActor(config, blockingIoPool, in, out));
    }

    private final ConsoleConfig config;
    private final InputStream is;
    private final OutputStream out;
    private final ExecutorService threadPool;
    private final ActorRef self;
    private ActorRef inRef;
    private ActorRef outRef;

    private ConsoleActor(final ConsoleConfig config, final ExecutorService blockingIpPool, final InputStream is, final OutputStream out) {
        super(true);
        this.config = config;
        this.is = is;
        this.out = out;
        threadPool = blockingIpPool;
        this.self = self();
    }

    @Override
    public void start() {
        logInfo("Starting");
        inRef = ctx().actorOf("in", InputStreamActor.props(is, threadPool, config.getInputStreamConfig()));
        outRef = ctx().actorOf("out", OutputStreamActor.props(out, threadPool, config.getOutputStreamConfig()));
    }

    @Override
    protected void onEvent(final Object msg) {
        if (msg instanceof StreamToken) {
            final StreamToken token = (StreamToken) msg;
            final Buffer noCRLF = token.getBuffer().stripEOL();
            tellSubscribers(StreamToken.of(noCRLF));
        } else if (msg instanceof IoEvent) {
            processIoEvent((IoEvent)msg);
        }
    }

    private void processIoEvent(final IoEvent event) {
        if (event.isWriteEvent()) {
            outRef.tell(event, self());
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
