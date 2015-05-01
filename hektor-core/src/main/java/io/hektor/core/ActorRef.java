package io.hektor.core;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ActorRef {

    /**
     * The unique path to this actor.
     *
     * @return
     */
    ActorPath path();

    void tell(Object msg, ActorRef sender);

    void tell(Object msg);
}
