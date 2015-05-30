package io.hektor.core.internal;

/**
 * @author jonas@jonasborjesson.com
 */
public interface MailBox {

    Envelope poll();

    void offer(Envelope envelope);

}
