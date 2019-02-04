package io.hektor.actors.io;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Configuration class to control the {@link OutputStreamActor}
 */
@JsonDeserialize(builder = InputStreamConfig.Builder.class)
public class InputStreamConfig {

    private final boolean parentAutoSubscribe;
    private final boolean stripEol;

    public static Builder of() {
        return new Builder();
    }

    public boolean isParentAutoSubscribe() {
        return parentAutoSubscribe;
    }

    public boolean isStripEol() {
        return stripEol;
    }

    private InputStreamConfig(final boolean parentAutoSubscribe, final boolean stripEol) {
        this.parentAutoSubscribe = parentAutoSubscribe;
        this.stripEol = stripEol;
    }

    public static final class Builder {

        /**
         * Dictates whether or not the parent actor (if there is one) will by default
         * be receiving all events from the {@link InputStreamActor}.
         */
        private boolean parentAutoSubscribe = true;
        private boolean stripEol = true;

        public Builder() {
            // empty intentionally...
        }

        public Builder withParentAutoSubscribe(final boolean value) {
            this.parentAutoSubscribe = value;
            return this;
        }

        public Builder withStripEol(final boolean value) {
            this.stripEol = value;
            return this;
        }

        public InputStreamConfig build() {
            return new InputStreamConfig(parentAutoSubscribe, stripEol);
        }
    }
}
