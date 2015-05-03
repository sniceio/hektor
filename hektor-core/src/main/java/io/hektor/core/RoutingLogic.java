package io.hektor.core;

import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public interface RoutingLogic {

    ActorRef select(Object msg, List<ActorRef> routees);
}
