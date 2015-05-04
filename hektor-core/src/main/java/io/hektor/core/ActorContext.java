package io.hektor.core;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ActorContext {

    void stash();

    void unstash();

    ActorRef actorOf(String name, Props props);

    ActorRef sender();

    ActorRef self();
}
