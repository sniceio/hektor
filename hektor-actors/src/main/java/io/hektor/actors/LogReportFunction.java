package io.hektor.actors;

/**
 * @author jonas@jonasborjesson.com
 */
@FunctionalInterface
public interface LogReportFunction {

    void apply(String format, Object... args);

}
