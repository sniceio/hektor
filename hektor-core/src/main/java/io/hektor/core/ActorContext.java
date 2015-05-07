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
    Optional<ActorRef> lookup(final String path);

    /**
     * TODO: do we want this?
     *
     * If you can get hold of the dispatcher you can schedule runnables in general and get a future back
     * or something.
     */
    // Dispatcher dispatcher();
}
