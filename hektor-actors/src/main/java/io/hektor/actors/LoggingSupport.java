package io.hektor.actors;

import org.slf4j.Logger;

public interface LoggingSupport {

    Logger getLogger();

    Object getUUID();

    default void logDebug(final String msg, final Object... args) {
        getLogger().debug("[{}] " + msg, mergeArgs(args));
    }

    default void logInfo(final String msg, final Object... args) {
        getLogger().info("[{}] " + msg, mergeArgs(args));
    }

    default void logInfo(final Alert alert, final Object... args) {
        log(getLogger()::info, alert, args);
    }

    default void logWarn(final Alert alert, final Object... args) {
        log(getLogger()::warn, alert, args);
    }

    default void logError(final Alert alert, final Object... args) {
        log(getLogger()::error, alert, args);
    }

    /**
     * Log an {@link AlertCode} using the supplied reporter function.
     *
     * This method is simply so that we can set the correct MDC context
     * in one single place.
     * @param reporter the function to use for the actual logging.
     * @param alert
     * @param args
     */
    default void log(final LogReportFunction reporter, final Alert alert, final Object... args) {
        final String logMsg = "AlertCode:{} [{}] " + alert.getMessage();
        reporter.apply(logMsg, mergeArgs(alert, args));
    }

    default Object[] mergeArgs(final AlertCode alert, final Object... args) {
        final Object[] merged = new Object[2 + (args != null ? args.length : 0)];
        merged[0] = alert.getCode();
        merged[1] = getUUID();
        for (int i = 2; i < merged.length; ++i) {
            merged[i] = args[i - 2];
        }
        return merged;
    }

    default Object[] mergeArgs(final Object... args) {
        final Object[] merged = new Object[1 + (args != null ? args.length : 0)];
        merged[0] = getUUID();
        for (int i = 1; i < merged.length; ++i) {
            merged[i] = args[i - 1];
        }
        return merged;
    }

}
