package io.hektor.core.internal;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultMailBox implements MailBox {

    private Queue<Envelope> messages = new LinkedList<>();

    public DefaultMailBox() {
        // left empty intentionally
    }

    @Override
    public synchronized Envelope poll() {
        return messages.poll();
    }

    @Override
    public synchronized void offer(final Envelope envelope) {
        if (envelope.isAsk()) {
            System.err.println("=====================");
        }
        messages.offer(envelope);
    }
}
