package io.hektor.core;

/**
 * A router is a special actor that
 * @author jonas@jonasborjesson.com
 */
public interface Router extends Actor {

    /**
     * The actor
     * @return
     */
    ActorRef ref();

}
