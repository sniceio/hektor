package io.hektor.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.hektor.config.HektorConfiguration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Basic tests to configure and start Hektor and send some basic messages through.
 *
 * @author jonas@jonasborjesson.com
 */
public class HektorTestBase {

    protected HektorConfiguration loadConfig(final String resourceName) throws IOException {
        final InputStream yamlStream = HektorTestBase.class.getResourceAsStream(resourceName);
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yamlStream, HektorConfiguration.class);
    }


}