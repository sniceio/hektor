package io.hektor.fsm;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Context {

    ThreadLocal<Scheduler> _scheduler = new ThreadLocal<>();

    default Scheduler getScheduler() {
        return _scheduler.get();
    }
}
