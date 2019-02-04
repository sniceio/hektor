package io.hektor.actors.events.subscription;

import io.hektor.core.ActorRef;

public interface SubscriptionManagementEvent {

    /**
     * Return the subscriber for whom we managing the subscription.
     *
     * @return
     */
    ActorRef getSubscriber();

    default boolean isSubscribeEvent() {
        return false;
    }

    default boolean isUnSubscribeEvent() {
        return false;
    }

}
