package io.hektor.actors.io;

import io.snice.buffer.Buffer;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface IoReadEvent extends IoEvent {

    static IoReadEvent of(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        return new DefaultReadEvent(buffer);
    }

    @Override
    default boolean isReadEvent() {
        return true;
    }

    @Override
    default IoReadEvent toReadEvent() {
        return this;
    }

    class DefaultReadEvent implements IoReadEvent {
        private final Buffer buffer;

        private DefaultReadEvent(final Buffer buffer) {
            this.buffer = buffer;
        }
    }
}
