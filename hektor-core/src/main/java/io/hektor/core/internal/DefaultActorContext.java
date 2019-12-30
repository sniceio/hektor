package io.hektor.core.internal;

import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.hektor.core.Request;
import io.hektor.core.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static io.hektor.core.internal.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultActorContext implements ActorContext {

    private final ActorBox self;

    private final ActorRef sender;

    private final InternalHektor hektor;

    private boolean stop;

    private final List<Envelope> bufferedMessages = new ArrayList<>(3);

    public DefaultActorContext(final InternalHektor hektor, final ActorBox self, final ActorRef sender) {
        assertNotNull(sender);
        this.hektor = hektor;
        this.self = self;
        this.sender = sender;
    }

    public void buffer(final Object msg, final ActorRef receiver, final ActorRef sender) {
        final Envelope envelope = new Envelope(sender, receiver, msg);
        bufferedMessages.add(envelope);
    }

    public void buffer(final ActorRef receiver, final ActorRef sender, final Request request) {
        final Envelope envelope = new Envelope(sender, receiver, request);
        bufferedMessages.add(envelope);
    }

    public void buffer(final ActorRef receiver, final ActorRef sender, final DefaultResponse response) {
        final Envelope envelope = new Envelope(sender, receiver, response);
        bufferedMessages.add(envelope);
    }

    public CompletionStage<Object> ask(final Object msg, final ActorRef receiver) {
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final Envelope envelope = new Envelope(Priority.NORMAL, ActorRef.None(), receiver, msg, future, null, null);
        bufferedMessages.add(envelope);
        return future;
    }

    public List<Envelope> bufferedMessages() {
        return bufferedMessages;
    }

    protected boolean isStopped() {
        return stop;
    }

    @Override
    public void stash() {

    }

    @Override
    public void unstash() {

    }

    @Override
    public ActorRef actorOf(final String name, final Props props) {
        final ActorRef child = hektor.actorOf(self.ref().path(), name, props);
        self.addChild(name, child);
        return child;
    }

    @Override
    public Optional<ActorRef> lookup(final String path) {
        final ActorPath actorPath = DefaultActorPath.create(self.ref().path(), path);
        return lookup(actorPath);
    }

    @Override
    public Optional<ActorRef> lookup(final ActorPath path) {
        return hektor.lookupActorBox(path).map(ActorBox::ref);
    }

    @Override
    public Scheduler scheduler() {
        return hektor.scheduler();
    }

    @Override
    public Optional<ActorRef> child(final String child) {
        return self.getChild(child);
    }

    @Override
    public ActorRef sender() {
        return sender;
    }

    @Override
    public ActorRef self() {
        return self.ref();
    }

    @Override
    public void stop() {
        stop = true;
    }
}
