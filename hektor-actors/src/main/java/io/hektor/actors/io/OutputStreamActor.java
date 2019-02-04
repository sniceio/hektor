package io.hektor.actors.io;

import io.hektor.actors.SubscriptionManagementSupport;
import io.hektor.core.Actor;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * An actor whose purpose it is to read from an input stream and emit
 * tokens and/or lines to a given {@link ActorRef}.
 *
 * Use this actor for dealing with blocking I/O in a non-blocking Actor kind of way.
 */
public class OutputStreamActor extends SubscriptionManagementSupport implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OutputStreamActor.class);
    private static final Buffer EOL = Buffers.wrap(Buffer.CR, Buffer.LF);

    /**
     * Create a new {@link Props} for the {@link OutputStreamActor}, which needs a receiver
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
     * @param out the output stream from where to read data.
     * @param threadPool the thread pool we will be using for dealing with reading from the stream since
     *                   that operation is actually blocking and we cannot block an actor.
     * @return
     */
    public static Props<OutputStreamActor> props(final OutputStream out, final ExecutorService threadPool) {
        final OutputStreamConfig config = OutputStreamConfig.of().build();
        return props(out, threadPool, config);
    }

    public static Props<OutputStreamActor> props(final OutputStream out, final ExecutorService threadPool, final OutputStreamConfig config) {
        assertNotNull(out, "The OutputStream cannot be null");
        assertNotNull(threadPool, "The threadpool cannot be null");
        assertNotNull(config, "The configuration for the OutputStreamActor cannot be null");
        return Props.forActor(OutputStreamActor.class, () -> new OutputStreamActor(config, out, threadPool));
    }

    private final OutputStreamConfig config;
    private final OutputStream out;
    private final ExecutorService threadPool;

    private final ActorRef self;

    private OutputStreamActor(final OutputStreamConfig config, final OutputStream out, final ExecutorService threadPool) {
        super(config.isParentAutoSubscribe());
        this.config = config;
        this.out = out;
        this.threadPool = threadPool;
        this.self = self();
    }

    @Override
    public void start() {
        logInfo("Starting");
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
    }

    @Override
    protected void onEvent(final Object msg) {
        if (msg instanceof IoWriteEvent) {
            internalWrite((IoWriteEvent)msg);
        }
    }

    private void internalWrite(final IoWriteEvent writeEvent) {
        try {
            final Buffer buffer = writeEvent.getData();
            buffer.writeTo(out);

            final Buffer append = config.getAppend();
            if (config.isAppend()) {
                append.writeTo(out);
            }

            if (config.isAppendEolIfNecessary()) {
                final boolean eol= append != null ? append.endsWithEOL() : buffer.endsWithEOL();
                if (!eol) {
                    EOL.writeTo(out);
                }
            }

            // TODO: configure when to flush
            if (config.alwaysFlush() || writeEvent.flush()) {
                out.flush();
            }
            // TODO: send a WriteSuccessfulEvent back to the caller.
        } catch (final IOException e) {
            // TODO: signal an error to the caller...
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
}
