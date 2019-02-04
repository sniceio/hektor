package io.hektor.actors.io;

import io.snice.buffer.Buffer;

import java.io.InputStream;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Represents a {@link StreamToken} that the {@link InputStreamActor} has read off of its
 * {@link InputStream}.
 */
public interface StreamToken {

    static StreamToken of(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        return new DefaultStreamToken(buffer);
    }

    /**
     * Get the {@link Buffer} containing the data that was read off of the underlying {@link InputStream}
     *
     * @return
     */
    Buffer getBuffer();

    /**
     * Indicate whether or not this is a full line
     *
     * @return
     */
    boolean isLine();

    class DefaultStreamToken implements StreamToken {

        private final Buffer buffer;

        private DefaultStreamToken(final Buffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public Buffer getBuffer() {
            return buffer;
        }

        @Override
        public boolean isLine() {
            return false;
        }
    }

}
