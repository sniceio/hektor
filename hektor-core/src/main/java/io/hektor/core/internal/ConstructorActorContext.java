package io.hektor.core.internal;

import io.hektor.core.ActorContext;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.hektor.core.Scheduler;

import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public class ConstructorActorContext implements ActorContext {

    private final ActorRef self;

    public ConstructorActorContext(final ActorRef self) {
        this.self = self;
    }

    @Override
    public void stash() {
        throw new IllegalStateException("You cannot stash messages from within a constructor");
    }

    @Override
    public void unstash() {
        throw new IllegalStateException("You cannot unstash messages from within a constructor");
    }

    @Override
    public ActorRef actorOf(String name, Props props) {
        throw new IllegalStateException("You cannot create children from constructor");
    }

    @Override
    public ActorRef sender() {
        return ActorRef.None();
    }

    @Override
    public ActorRef self() {
        return self;
    }

    @Override
    public void stop() {
        throw new IllegalStateException("You cannot stop yourself when being constructed");
    }

    @Override
    public Optional<ActorRef> lookup(final String path) {
        // TODO: implement
        throw new RuntimeException("Sorry, not implemented just yet");
    }

    @Override
    public Scheduler scheduler() {
        throw new RuntimeException("need to implmenet this for the constructor context as well");
    }
}
