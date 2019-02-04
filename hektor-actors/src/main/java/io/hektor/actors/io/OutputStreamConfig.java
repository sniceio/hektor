package io.hektor.actors.io;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.snice.buffer.Buffer;

import java.io.OutputStream;

/**
 * Configuration class to control the {@link OutputStreamActor}
 */
@JsonDeserialize(builder = OutputStreamConfig.Builder.class)
public class OutputStreamConfig {

    private final Buffer append;
    private final boolean parentAutoSubscribe;
    private final boolean alwaysFlush;

    /**
     * Unless what we are writing is already ending with EOL or if the append buffer
     * contains EOL we will append that if this flag is set to true.
     */
    private final boolean appendEolIfNecessary;

    public static Builder of() {
        return new Builder();
    }

    public boolean isParentAutoSubscribe() {
        return parentAutoSubscribe;
    }

    public boolean alwaysFlush() {
        return alwaysFlush;
    }

    public boolean isAppend() {
        return append != null;
    }

    public boolean isAppendEolIfNecessary() {
        return appendEolIfNecessary;
    }

    public Buffer getAppend() {
        return append;
    }

    private OutputStreamConfig(final boolean parentAutoSubscribe,
                               final boolean flush,
                               final Buffer appendToOutput,
                               final boolean appendEolIfNecessary) {
        this.parentAutoSubscribe = parentAutoSubscribe;
        this.alwaysFlush = flush;
        this.append = appendToOutput;
        this.appendEolIfNecessary = appendEolIfNecessary;
    }

    public static final class Builder {

        private Buffer append;
        private boolean flush;

        /**
         * Dictates whether or not the parent actor (if there is one) will by default
         * be receiving all events from the {@link OutputStreamActor}.
         */
        private boolean parentAutoSubscribe = true;

        private boolean appendEolIfNecessary = true;

        private Builder() {
            // empty intentionally...
        }

        public Builder withParentAutoSubscribe(final boolean value) {
            this.parentAutoSubscribe = value;
            return this;
        }

        public Builder withAppendEolIfNecessary(final boolean value) {
            this.appendEolIfNecessary = value;
            return this;
        }

        /**
         * Whether or not we should call {@link OutputStream#flush()} after every write operation.
         * If not set, then the individual {@link IoWriteEvent} needs to let us know when to
         * issue a flush.
         *
         * @param value
         * @return
         */
        public Builder withAlwaysFlush(final boolean value) {
            this.flush = value;
            return this;
        }

        public Builder withAppend(final Buffer buffer) {
            this.append = buffer;
            return this;
        }

        public OutputStreamConfig build() {
            return new OutputStreamConfig(parentAutoSubscribe, flush, append, appendEolIfNecessary);
        }
    }
}
