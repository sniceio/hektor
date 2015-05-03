package io.hektor.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.hektor.config.HektorConfiguration;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Basic tests to configure and start Hektor and send some basic messages through.
 *
 * @author jonas@jonasborjesson.com
 */
public class HektorTestBase {

    protected Hektor defaultHektor;

    @Before
    public void setUp() throws Exception {
        defaultHektor = initHektor("hektor_config.yaml");
    }

    protected Hektor initHektor(final String configResourceName) throws IOException {
        final HektorConfiguration config = loadConfig(configResourceName);
        final Hektor hektor = Hektor.withName("hello").withConfiguration(config).build();
        assertThat(hektor, not((Hektor) null));
        return hektor;
    }

    protected HektorConfiguration loadConfig(final String resourceName) throws IOException {
        final InputStream yamlStream = HektorTestBase.class.getResourceAsStream(resourceName);
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yamlStream, HektorConfiguration.class);
    }


}