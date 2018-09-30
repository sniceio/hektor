/**
 *
 */
package io.hektor.fsm;

/**
 * Contains common checks for null etc. All checkXXX will
 * return a boolean and all ensureXXX will throw an {@link IllegalArgumentException}.
 *
 * @author jonas@jonasborjesson.com
 */
public final class PreConditions {

    private PreConditions() {
    }

    public static <T> T ensureNotNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static <T> T ensureNotNull(final T reference) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return reference;
    }

    /**
     * Check if a string is empty, which includes null check.
     *
     * @param string
     * @return true if the string is either null or empty
     */
    public static boolean checkIfEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean checkIfNotEmpty(final String string) {
        return !checkIfEmpty(string);
    }

    public static String ensureNotEmpty(final String reference, final String msg) throws IllegalArgumentException {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static void ensureArgument(final boolean expression, final String msg) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(msg);
        }
    }


    /**
     * If our reference is null then return a default value instead.
     *
     * @param reference    the thing to check.
     * @param defaultValue the default value to return if the above reference is null.
     * @return the reference if not null, otherwise the default value. Note, if your default value
     * is null as well then you will get back null, since that is what you asked. Chain with
     * {@link #ensureNotNull(Object, String)} if you want to make sure you have a non-null
     * value for the default value.
     */
    public static <T> T ifNull(final T reference, final T defaultValue) {
        if (reference == null) {
            return defaultValue;
        }
        return reference;
    }

}
