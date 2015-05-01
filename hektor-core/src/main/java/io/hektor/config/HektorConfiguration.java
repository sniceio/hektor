package io.hektor.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The main class for all Hektor related configuration.
 *
 * @author jonas@jonasborjesson.com
 */
public class HektorConfiguration {

    /**
     * A map of all configured dispatchers. If no dispatchers have been
     * specified either through code or via configuration then when
     * you build a new Hektor instance, a default dispatcher configuration
     * will be added.
     */
    @JsonProperty
    private final Map<String, DispatcherConfiguration> dispatchers = new HashMap<>();

    public Map<String, DispatcherConfiguration> dispatchers() {
        return Collections.unmodifiableMap(dispatchers);
    }
}
