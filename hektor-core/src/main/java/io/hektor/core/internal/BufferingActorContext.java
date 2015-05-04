package io.hektor.core.internal;

import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.hektor.core.internal.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public class BufferingActorContext implements ActorContext {

    private final ActorBox self;
    private final BufferingActorRef bufferingSelf;

    private final ActorRef sender;
    private final BufferingActorRef bufferingSender;

    private final InternalDispatcher dispatcher;

    public BufferingActorContext(final InternalDispatcher dispatcher, final ActorBox self, final ActorRef sender) {
        assertNotNull(sender);
        this.dispatcher = dispatcher;

        this.self = self;
        this.bufferingSelf = new BufferingActorRef(self.ref());

        this.sender = sender;
        this.bufferingSender = new BufferingActorRef(sender);
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
        return null;
    }

    @Override
    public ActorRef sender() {
        return bufferingSender;
    }

    @Override
    public ActorRef self() {
        return bufferingSelf;
    }

    private static class BufferingActorRef implements ActorRef {

        private final ActorRef self;
        private List<Envelope> messages;

        public BufferingActorRef(final ActorRef self) {
            System.err.println("I dont fucking get it. why is this null " + self);
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
