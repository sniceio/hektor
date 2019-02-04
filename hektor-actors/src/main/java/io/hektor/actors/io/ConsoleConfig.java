package io.hektor.actors.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Configuration options for the {@link ConsoleActor}.
 */
@JsonDeserialize(builder = ConsoleConfig.Builder.class)
public class ConsoleConfig {

    public static Builder of() {
        return new Builder();
    }

    @JsonProperty("out")
    private final OutputStreamConfig outConfig;

    @JsonProperty("in")
    private final InputStreamConfig inConfig;

    private ConsoleConfig(final InputStreamConfig inConfig, final OutputStreamConfig outConfig) {
        this.inConfig = inConfig;
        this.outConfig = outConfig;
    }

    public OutputStreamConfig getOutputStreamConfig() {
        return outConfig;
    }

    public InputStreamConfig getInputStreamConfig() {
        return inConfig;
    }

    public static class Builder {

        private OutputStreamConfig outConfig;
        private InputStreamConfig inConfig;

        public Builder() {
            // left empty so that jackson can create an
            // instance of this builder.
        }

        @JsonProperty("out")
        public Builder withOutputStreamConfig(final OutputStreamConfig config) {
            assertNotNull(config, "The output configuration cannot be null");
            outConfig = config;
            return this;
        }

        @JsonProperty("in")
        public Builder withInputStreamConfig(final InputStreamConfig config) {
            assertNotNull(config, "The input configuration cannot be null");
            inConfig = config;
            return this;
        }

        public ConsoleConfig build() {
            return new ConsoleConfig(ensureInputConfig(), ensureOutputConfig());
        }

        private InputStreamConfig ensureInputConfig() {
            if (inConfig != null) {
                return inConfig;
            }

            return InputStreamConfig.of().withParentAutoSubscribe(true).build();
        }

        private OutputStreamConfig ensureOutputConfig() {
            if (outConfig != null) {
                return outConfig;
            }

            return OutputStreamConfig.of().withParentAutoSubscribe(true).withAlwaysFlush(true).build();
        }
    }

}
