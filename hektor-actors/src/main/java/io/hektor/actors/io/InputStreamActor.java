package io.hektor.actors.io;

import io.hektor.actors.SubscriptionManagementSupport;
import io.hektor.core.Actor;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.snice.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * An actor whose purpose it is to read from an input stream and emit
 * tokens and/or lines to a given {@link ActorRef}.
 *
 * Use this actor for dealing with blocking I/O in a non-blocking Actor kind of way.
 */
public class InputStreamActor extends SubscriptionManagementSupport implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(InputStreamActor.class);

    /**
     * Create a new {@link Props} for the {@link InputStreamActor}, which needs a receiver
     * for where to send the tokens and lines in reads off of the given input stream.
     *
     * Since reading off of an {@link InputStream} is a blocking operation and we cannot
     * block the {@link Actor} ever so therefore we will be using a different thread pool for the
     * task of reading off of the {@link InputStream}.
     *
     * Note, the thread pool you supply ({@link ExecutorService})
     * should be a pool whose purpose is to deal with blocking operations. Typically you would size this
     * pool to be fairly large and should always be different from the pool your program is using for
     * CPU non-blocking operations.
     *
     *
     * @param is the input stream from where to read data.
     * @param threadPool the thread pool we will be using for dealing with reading from the stream since
     *                   that operation is actually blocking and we cannot block an actor.
     * @return
     */
    public static Props<InputStreamActor> props(final InputStream is, final ExecutorService threadPool) {
        assertNotNull(is, "The InputStream cannot be null");
        assertNotNull(threadPool, "The threadpool cannot be null");
        final InputStreamConfig config = InputStreamConfig.of().build();
        return props(is, threadPool, config);
    }

    public static Props<InputStreamActor> props(final InputStream is, final ExecutorService threadPool, final InputStreamConfig config) {
        assertNotNull(is, "The InputStream cannot be null");
        assertNotNull(threadPool, "The threadpool cannot be null");
        assertNotNull(config, "The configuration cannot be null");
        return Props.forActor(InputStreamActor.class, () -> new InputStreamActor(config, is, threadPool));
    }

    private final InputStreamConfig config;
    private final InputStream is;
    private final ExecutorService threadPool;

    private final ActorRef self;

    // too small and you will have to issue lots of small events, too
    // big and you're wasting memory...
    private final int MAX_BUFFER_SIZE = 100;

    private int countExceptions;

    private InputStreamActor(final InputStreamConfig config, final InputStream is, final ExecutorService threadPool) {
        super(config.isParentAutoSubscribe());
        this.config = config;
        this.is = is;
        this.threadPool = threadPool;
        this.self = self();
    }

    @Override
    public void start() {
        logInfo("Starting");
        threadPool.submit(this);
    }

    @Override
    protected void onEvent(final Object msg) {
        if (msg instanceof StreamToken) {
            tellSubscribers(msg);

            // as long as we keep reading we'll keep asking for more...
            threadPool.submit(this);
        } else if (msg == EndOfStream.EOF) {
            ctx().stop();
        } else if (msg instanceof IOException) {
            // in case there was some odd stuff, let's try again.
            // If someone external closed the stream, we will fail and
            // then give up.
            if (++countExceptions < 2) {
                threadPool.submit(this);
            } else {
                ctx().stop();
            }
        }

    }

    @Override
    public void stop() {
        logInfo("Stopping");
    }

    @Override
    public void postStop() {
        logInfo("Stopped");
    }

    @Override
    public void run() {
        final byte[] buf = new byte[MAX_BUFFER_SIZE];
        try {
            final int count = is.read(buf, 0, buf.length);

            if (count == -1) {
                self.tell(EndOfStream.EOF);
            } else {
                self.tell(StreamToken.of(Buffer.of(buf, 0, count)));
            }
        } catch (final IOException e) {
            self.tell(e);
        } catch (final Exception e) {
            e.printStackTrace();
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

    private static final class EndOfStream {
        private static final EndOfStream EOF = new EndOfStream();
    }


}
