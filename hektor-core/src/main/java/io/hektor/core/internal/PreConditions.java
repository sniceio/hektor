package io.hektor.core.internal;

/**
 * @author jonas@jonasborjesson.com
 */
public class PreConditions {

    public static <T> T assertNotNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static <T> T assertNotNull(final T reference) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return reference;
    }
}
