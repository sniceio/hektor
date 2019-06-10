package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;
import io.hektor.core.internal.messages.Stop;
import io.hektor.core.internal.messages.Watch;
import io.netty.util.internal.ConcurrentSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author jonas@jonasborjesson.com
 */
public class ActorBox {

    private final Actor actor;

    private final ActorRef ref;

    /**
     * Whenever the user asks an actor to stop we have a bunch of cleanup tasks
     * to accomplish before we actually get to the point of stop completely.
     * Until then, we do not e.g. want to process any new messages and this
     * flag keeps track of if we are in shutting down state.
     */
    private boolean isStopped = false;

    private final MailBox mailBox;

    /**
     * A map of all the children that has belongs to this actor.
     * Remember, it is guaranteed that the ActorBox is only
     * accessed in a "thread safe" manner so no need to
     * have any additional locks/concurrent maps or whatever.
     *
     * The current execution context MUST guarantee this.
     */
    private final Map<String, ActorRef> children = new HashMap<>();

    /**
     * A list of other actors that is monitoring this one for life-cycle events.
     */
    private Set<ActorRef> watchers;

    private ActorBox(final MailBox mailBox, final Actor actor, final ActorRef ref) {
        this.mailBox = mailBox;
        this.actor = actor;
        this.ref = ref;
    }

    public static ActorBox create(final MailBox mailBox, final Actor actor, final ActorRef ref) {
        return new ActorBox(mailBox, actor, ref);
    }

    public void tellWatchers(final Object msg) {
        if (watchers == null) {
            return;
        }

        watchers.forEach(watcher -> {
            watcher.tell(msg, ref);
        });

    }

    /**
     * Return the mail box belonging to this actor.
     *
     * @return
     */
    public MailBox mailBox() {
        return mailBox;
    }

    /**
     * Send a stop message to all our children (if any)
     */
    public void stopChildren() {
        children.values().forEach(child -> {
            child.tell(Stop.MSG, ref);
        });
    }

    public void addChild(final String name, final ActorRef ref) {
        children.put(name, ref);
    }

    public Optional<ActorRef> getChild(final String name) {
        return Optional.ofNullable(children.get(name));
    }

    public boolean hasChild(final String name) {
        return children.containsKey(name);
    }

    /**
     * Remove a child and return the total number of children this parent still has.
     */
    public int removeChild(final String name) {
        children.remove(name);
        return children.size();
    }

    public void watch(final ActorRef watcher) {
        if (watchers == null) {
            watchers = new HashSet<>();
        }
        watchers.add(watcher);
    }

    public boolean hasNoChildren() {
        return children.isEmpty();
    }

    /**
     * To indicate that this actor is being stopped, call this method.
     */
    public void stop() {
        isStopped = true;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public ActorRef ref() {
        return ref;
    }

    public Actor actor() {
        return actor;
    }
}
