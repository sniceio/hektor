package io.hektor.actors.events.subscription;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;
import io.snice.preconditions.PreConditions;

/**
 * There are many actors within Hektor that allows others to subscribe to events
 * from that actor. All the {@link Actor}s within Hektor that allows for some kind
 * of subscription use this event to set that subscription up.
 */
public final class SubscribeEvent implements SubscriptionManagementEvent {

    private final ActorRef subscriber;

    public static SubscribeEvent subscriber(final ActorRef subscriber){
        PreConditions.assertNotNull(subscriber, "The subscriber cannot be null");
        return new SubscribeEvent(subscriber);
    }

    private SubscribeEvent(final ActorRef subscriber) {
        this.subscriber = subscriber;
    }

    public ActorRef getSubscriber() {
        return subscriber;
    }

    public boolean isSubscribeEvent() {
        return true;
    }
}
