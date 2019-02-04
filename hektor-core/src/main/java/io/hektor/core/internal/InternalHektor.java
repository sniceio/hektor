package io.hektor.core.internal;

import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.hektor.core.Props;

import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public interface InternalHektor extends Hektor {

    Optional<ActorBox> lookupActorBox(ActorRef ref);

    Optional<ActorBox> lookupActorBox(ActorPath path);

    /**
     * Lookup the reference of an actor based on its absolute path expressed as a string.
     *
     * @param path
     * @return
     * @throws IllegalArgumentException in case the supplied path is not an absolute path
     */
    Optional<ActorBox> lookupActorBox(String path) throws IllegalArgumentException;

    /**
     * Create a child actor.
     *
     * @param parent
     * @param name
     * @param props
     * @return
     */
    ActorRef actorOf(ActorPath parent, String name, Props props);

    /**
     * Remove an actor completely from the system. Once this has been called,
     * there is no way to get back the actor.
     *
     * @param ref
     */
    void removeActor(ActorRef ref);

    /**
     * Setup a one actor to watch the life-cycle event of another.
     *
     * @param watcher the watcher that wishes to monitor the target
     * @param target the actor we are monitoring.
     * @return boolean indicating whether setting up the watch was successful. This would return
     * false if e.g. the target Actor doesn't exist (perhaps it once did but it does no more).
     */
    boolean watch(ActorRef watcher, ActorRef target);
}
