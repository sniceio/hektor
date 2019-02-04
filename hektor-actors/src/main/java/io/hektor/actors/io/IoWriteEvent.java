package io.hektor.actors.io;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;

import static io.snice.preconditions.PreConditions.assertNotNull;

interface IoWriteEvent extends IoEvent {

    static IoWriteEvent of(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        return new DefaultIoWriteEvent(buffer);
    }

    static IoWriteEvent of(final String str) {
        assertNotNull(str, "The String cannot be null (allowed to be empty though)");
        return new DefaultIoWriteEvent(Buffers.wrap(str));
    }

    /**
     * By default, we will flush after every write event.
     * @return
     */
    default boolean flush() {
        return true;
    }

    /**
     * Get the data that we are supposed to write to the output stream.
     * @return
     */
    Buffer getData();

    @Override
    default boolean isWriteEvent() {
        return true;
    }

    @Override
    default IoWriteEvent toWriteEvent() {
        return this;
    }

    class DefaultIoWriteEvent implements IoWriteEvent {
        private final Buffer buffer;

        private DefaultIoWriteEvent(final Buffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public Buffer getData() {
            return buffer;
        }
    }
}
