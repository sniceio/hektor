package io.hektor.core.internal;

import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.hektor.core.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.hektor.core.internal.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public class BufferingActorContext implements ActorContext {

    private final ActorBox self;
    private final BufferingActorRef bufferingSelf;

    private final ActorRef sender;
    private final BufferingActorRef bufferingSender;

    private final InternalHektor hektor;

    private boolean stop;

    public BufferingActorContext(final InternalHektor hektor, final ActorBox self, final ActorRef sender) {
        assertNotNull(sender);
        this.hektor = hektor;

        this.self = self;
        this.bufferingSelf = new BufferingActorRef(self.ref());

        this.sender = sender;
        this.bufferingSender = new BufferingActorRef(sender);
    }

    protected boolean isStopped() {
        return stop;
    }

    protected List<Envelope> messages() {
        final int count = bufferingSelf.messagesToSend() + bufferingSender.messagesToSend();
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
        return child;
    }

    @Override
    public Optional<ActorRef> lookup(final String path) {
        final ActorPath actorPath = DefaultActorPath.create(self.ref().path(), path);
        return hektor.lookupActorBox(actorPath).map(ActorBox::ref);
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
        public void tell(final Object msg, final ActorRef sender) {
            ensureList();
            final Envelope envelope = new Envelope(sender, self, msg);
            messages.add(envelope);
        }

        private void ensureList() {
            if (messages == null) {
                messages = new ArrayList<>(3);
            }
        }

        @Override
        public void tellAnonymously(Object msg) {

        }
    }
}
