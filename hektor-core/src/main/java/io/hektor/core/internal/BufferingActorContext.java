package io.hektor.core.internal;

import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.hektor.core.Scheduler;
import io.snice.protocol.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static io.hektor.core.internal.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public class BufferingActorContext implements ActorContext {

    private final ActorBox self;
    private final BufferingActorRef bufferingSelf;

    private final ActorRef sender;
    private final BufferingActorRef bufferingSender;

    /**
     * An actor can of course do ctx().actorOf(...).tell(msg, self()) and
     * we must naturally process those events as well.
     */
    private List<BufferingActorRef> children;

    /**
     * We will intercept all attempts to send messages between actors
     * during an actor invocation. The DefaultActorRef will
     */
    private final List<Envelope> messages;

    private final InternalHektor hektor;

    private boolean stop;

    public BufferingActorContext(final InternalHektor hektor, final ActorBox self, final ActorRef sender) {
        assertNotNull(sender);
        this.hektor = hektor;

        this.self = self;
        this.bufferingSelf = new BufferingActorRef(self.ref());

        this.sender = sender;
        this.bufferingSender = new BufferingActorRef(sender);

        messages = new ArrayList<>(5);
    }

    /**
     * Because when the actor sends a message to one actor to another, that actor
     * will end up using the BufferingActorRef, which of course doesn't work when
     * we actually need to send it for real. This method unwraps that.
     *
     * @param ref
     * @return
     */
    public ActorRef unWrapRef(final ActorRef ref) {
        if (ref == bufferingSender) {
            return sender;
        }

        if (ref == bufferingSelf) {
            return self.ref();
        }

        return ref;
    }

    protected boolean isStopped() {
        return stop;
    }

    public void dispatch(final Object msg, final ActorRef receiver, final ActorRef sender) {

    }

    protected List<Envelope> messages() {
        int count = bufferingSelf.messagesToSend() + bufferingSender.messagesToSend();
        if (children != null) {
            count += children.stream().mapToInt(child -> child.messagesToSend()).sum();
        }

        if (count == 0) {
            return Collections.emptyList();
        }

        final List<Envelope> messages = new ArrayList<>(count);
        if (bufferingSelf.messages() != null) {
            messages.addAll(bufferingSelf.messages());
        }

        if (bufferingSender.messages() != null) {
            messages.addAll(bufferingSender.messages());
        }

        if (children != null) {
            children.stream()
                    .filter(c -> c.messages() != null)
                    .forEach(c -> messages.addAll(c.messages()));
        }

        return messages;
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
        if (children == null) {
            children = new ArrayList<>(3);
        }

        final BufferingActorRef bufferingRef = new BufferingActorRef(child);
        children.add(bufferingRef);
        return bufferingRef;
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
        return bufferingSender;
    }

    @Override
    public ActorRef self() {
        return bufferingSelf;
    }

    @Override
    public void stop() {
        stop = true;
    }

    private static class BufferingActorRef implements ActorRef {

        private final ActorRef self;

        private List<Envelope> messages;

        public BufferingActorRef(final ActorRef self) {
            if (self instanceof BufferingActorRef) {
                throw new RuntimeException();
            }
            assertNotNull(self);
            this.self = self;
        }

        public int messagesToSend() {
            return messages == null ? 0 : messages.size();
        }

        public List<Envelope> messages() {
            return messages;
        }

        @Override
        public ActorPath path() {
            return self.path();
        }

        @Override
        public CompletionStage<Object> ask(final Object msg, final ActorRef sender) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public <T> Request<ActorRef, T> request(final ActorRef sender, final T msg) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public <T> Request<ActorRef, T> request(final Request<ActorRef, T> request) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public void tell(final Object msg, final ActorRef sender) {
            tell(Priority.NORMAL, msg, sender);
        }

        @Override
        public void tell(final Priority priority, final Object msg, final ActorRef sender) {
            ensureList();

            // make sure to unwrap the sender if it is of
            // type a buffering actor ref.
            // final Envelope envelope = new Envelope(sender instanceof BufferingActorRef ? ((BufferingActorRef)sender).self : sender, self, msg);
            final Envelope envelope = new Envelope(priority, sender, self, msg);
            messages.add(envelope);

        }

        private void ensureList() {
            if (messages == null) {
                messages = new ArrayList<>(3);
            }
        }

        @Override
        public void tellAnonymously(final Object msg) {
            throw new RuntimeException("Sorry, not implemented just yet");

        }

        @Override
        public void monitor(final ActorRef ref) {
            throw new RuntimeException("Sorry, not implemented just yet");
        }
    }
}
