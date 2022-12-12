package io.hektor.actors;

import io.hektor.actors.events.subscription.SubscriptionManagementEvent;
import io.hektor.core.Actor;
import io.hektor.core.ActorRef;
import io.snice.logging.Logging;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Helper base class for those {@link Actor}s that does support subscription type of
 * capabilities.
 */
public abstract class SubscriptionManagementSupport implements Actor, Logging {

    private final Set<ActorRef> subscribers = new HashSet<>();

    protected SubscriptionManagementSupport(final boolean parentAutoSubscribe) {
        if (parentAutoSubscribe && self().path().parent().isPresent()) {
            self().path().parent().ifPresent(parent -> {
                final Optional<ActorRef> parentActor = ctx().lookup(parent);
                parentActor.ifPresent(this::subscribe);
            });
        }
    }
    /**
     * Sub-classes must override this because we need to dispatch the message
     * if it wasn't a {@link SubscriptionManagementEvent}, which most of the time
     * it won't be so...
     *
     * @param msg
     */
    protected abstract void onEvent(final Object msg);

    /**
     * Send the message to all my subscribers..
     *
     * @param msg
     */
    protected void tellSubscribers(final Object msg) {
        subscribers.forEach(s -> s.tell(msg, self()));
    }

    private void subscribe(final ActorRef ref) {
        if (subscribers.add(ref)) {
            logInfo("Added {} as a subscriber", ref);
        }
    }

    @Override
    public final void onReceive(final Object msg) {

        if (msg instanceof SubscriptionManagementEvent) {
            processSubscriptionManagementEvent((SubscriptionManagementEvent)msg);
        } else {
            onEvent(msg);
        }
    }

    private void processSubscriptionManagementEvent(final SubscriptionManagementEvent event) {

        if (event.isSubscribeEvent()) {
            subscribe(event.getSubscriber());
        } else if (event.isUnSubscribeEvent()) {
            subscribers.remove(event.getSubscriber());
        }

    }

}
