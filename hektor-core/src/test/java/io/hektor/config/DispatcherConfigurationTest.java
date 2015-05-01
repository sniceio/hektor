package io.hektor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author jonas@jonasborjesson.com
 */
public class DispatcherConfigurationTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void loadConfiguration() throws Exception {
        final InputStream yamlStream = DispatcherConfigurationTest.class.getResourceAsStream("simple_config.yaml");
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final HektorConfiguration config = mapper.readValue(yamlStream, HektorConfiguration.class);
        assertThat(config.dispatchers().size(), is(1));

        final DispatcherConfiguration dispatcher = config.dispatchers().get("my-dispatcher");
        assertThat(dispatcher.executor(), is("worker-thread-executor"));
        assertThat(dispatcher.throughput(), is(75));
        assertThat(dispatcher.workerThreadExecutorConfig().isPresent(), is(true));

        final WorkerThreadExecutorConfig executorConfig = dispatcher.workerThreadExecutorConfig().get();
        assertThat(executorConfig.getNoOfWorkers(), is(4));
    }
}