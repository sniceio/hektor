package io.hektor.core;

/**
 * @author jonas@jonasborjesson.com
 */
public class NoSuchConstructorException extends RuntimeException {

    public NoSuchConstructorException(final String msg) {
        super(msg);
    }

}
