package io.hektor.benchmark.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.hektor.benchmark.warmup.WarmUpScenario;
import io.hektor.config.HektorConfiguration;
import io.hektor.core.Hektor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public class Benchmark {

    public static InputStream getConfiguration() {
        return Benchmark.class.getResourceAsStream("simple_benchmark.yaml");
    }

    public static Hektor initHektor(final InputStream yamlStream) throws IOException {
        final HektorConfiguration config = loadConfig(yamlStream);
        final Hektor hektor = Hektor.withName("hello").withConfiguration(config).build();
        return hektor;
    }

    public static HektorConfiguration loadConfig(final InputStream yamlStream) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yamlStream, HektorConfiguration.class);
    }

    public static void main(final String...args) throws Exception {
        final Hektor hektor = initHektor(getConfiguration());
        WarmUpScenario warmUp = new WarmUpScenario(hektor);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        warmUp.warmUp(100);
        System.err.println("Ok, resting...");
        Thread.sleep(5000);
        System.err.println("Ok, real test starting now...");
        final long ts = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            warmUp.warmUp(100);
        }
        System.err.println("Done " + (System.currentTimeMillis() - ts));
    }

}
