package io.hektor.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.hektor.config.HektorConfiguration;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Basic tests to configure and start Hektor and send some basic messages through.
 *
 * @author jonas@jonasborjesson.com
 */
public class HektorTestBase {

    protected Hektor defaultHektor;

    /**
     * We use latches all the time so let's create a few fresh
     * ones automatically for every new test run.
     */
    protected CountDownLatch defaultLatch1;

    /**
     * The default stop latch used by the default implementation of
     * ParentActor and will be called when the actor is asked to stop
     * itself.
     */
    protected CountDownLatch defaultStopLatch1;

    /**
     *
     */
    protected CountDownLatch defaultPostStopLatch1;

    protected CountDownLatch defaultTerminatedLatch1;

    protected CountDownLatch defaultLatch2;

    protected CountDownLatch defaultLatch3;

    @Before
    public void setUp() throws Exception {
        defaultHektor = initHektor("hektor_config.yaml");
        defaultLatch1 = new CountDownLatch(1);
        defaultStopLatch1 = new CountDownLatch(1);
        defaultPostStopLatch1 = new CountDownLatch(1);
        defaultTerminatedLatch1 = new CountDownLatch(1);

        defaultLatch2 = new CountDownLatch(1);
        defaultLatch3 = new CountDownLatch(1);
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