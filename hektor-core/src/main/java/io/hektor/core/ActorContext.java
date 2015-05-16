package io.hektor.core;

import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ActorContext {

    void stash();

    void unstash();

    ActorRef actorOf(String name, Props props);

    ActorRef sender();

    ActorRef self();

    /**
     * Stop the current actor, i.e. stop yourself.
     *
     * An actor can be stopped in two ways, either by calling this method or by sending
     * Stop message to the actor. Either way, when an actor is asked to stop its execution
     * the following will happen:
     *
     * <ul>
     *     <li>It will no longer accept any new messages.</li>
     *     <li>It will ask all its children to stop as well</li>
     *     <li>Once there are no more children it will finally terminate and send the Terminated message to its watchers</li>
     *     <li>All references to the actor instances will be released and subsequently the actor will be purged from memory</li>
     * </ul>
     */
    void stop();

    /**
     * Lookup the reference to an actor given the specified path. The path can be
     * absolute or relative. E.g.:
     *
     * <pre>
     *     context.lookup("/parent/brother")
     * </pre>
     *
     * <pre>
     *     context.lookup("../brother")
     * </pre>
     *
     * @param path
     * @return
     */
    Optional<ActorRef> lookup(String path);

    Scheduler scheduler();

    /**
     * Convenience method to look up a child.
     *
     * @param child
     * @return
     */
    Optional<ActorRef> child(String child);

    /**
     * TODO: do we want this?
     *
     * If you can get hold of the dispatcher you can schedule runnables in general and get a future back
     * or something.
     */
    // Dispatcher dispatcher();
}
