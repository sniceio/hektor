package io.hektor.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.hektor.config.HektorConfiguration;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
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

    protected InputStream loadStream(final String resourceName) throws IOException {
        return HektorTestBase.class.getResourceAsStream(resourceName);
    }

    protected String loadTextFile(final String resourceName) throws Exception {
        final Path path = Paths.get(HektorTestBase.class.getResource(resourceName).toURI());
        return new String(Files.readAllBytes(path));
    }

    protected HektorConfiguration loadConfig(final String resourceName) throws IOException {
        final InputStream yamlStream = HektorTestBase.class.getResourceAsStream(resourceName);
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yamlStream, HektorConfiguration.class);
    }

    protected ActorRef createDefaultParentTwoChildren() throws Exception {
        return createDefaultParentTwoChildrenWithTerminatedLatch(defaultTerminatedLatch1);
    }

    /**
     * Convenience method for creating two children as well as specifying the terminated latch.
     *
     * @param terminatedLatch the terminated latch will be used by the parent as a countdown latch for any
     *                        terminated events it may receive.
     * @return
     * @throws Exception
     */
    protected ActorRef createDefaultParentTwoChildrenWithTerminatedLatch(final CountDownLatch terminatedLatch) throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        final ActorRef ref = createParentActor(latch, defaultStopLatch1, defaultPostStopLatch1, terminatedLatch);
        ref.tellAnonymously(new ParentActor.CreateChildMessage("romeo"));
        ref.tellAnonymously(new ParentActor.CreateChildMessage("julia"));

        // wait to ensure that the parent actor got
        // both messages to create new children
        latch.await();

        // make sure that we now have to child actors
        assertThat(defaultHektor.lookup("./parent/julia").isPresent(), is(true));
        assertThat(defaultHektor.lookup("./parent/romeo").isPresent(), is(true));

        return ref;
    }

    /**
     * Super simple routing logic that assumes that the message is an Integer and
     * uses it as an index into the list of routees when selecting which actor
     * should get the message.
     */
    protected static class SimpleRoutingLogic implements RoutingLogic {
        @Override
        public ActorRef select(Object msg, List<ActorRef> routees) {
            Integer index = (Integer)msg;
            return routees.get(index);
        }
    }

    /**
     * Convenience method for creating a parent actor under the name "parent" and that is
     * using the default latch no 1.
     *
     * @return
     * @throws Exception
     */
    protected ActorRef createParentActor() throws Exception {
        return createParentActor(defaultLatch1, defaultStopLatch1, defaultPostStopLatch1, defaultTerminatedLatch1);
    }

    /**
     * Convenience method for creating a parent actor under the name "parent" and is using
     * the supplied latch.
     *
     * @param latch
     * @return
     * @throws Exception
     */
    protected ActorRef createParentActor(final CountDownLatch latch) throws Exception {
        return createParentActor(latch, defaultStopLatch1, defaultPostStopLatch1, defaultTerminatedLatch1);
    }

    protected ActorRef createParentActor(final CountDownLatch latch,
                                       final CountDownLatch stopLatch,
                                       final CountDownLatch postStopLatch) throws Exception {
        return createParentActor(latch, stopLatch, postStopLatch, defaultTerminatedLatch1);
    }

    protected ActorRef createParentActor(final CountDownLatch latch,
                                       final CountDownLatch stopLatch,
                                       final CountDownLatch postStopLatch,
                                       final CountDownLatch terminatedLatch) throws Exception {
        final Props props =
                Props.forActor(ParentActor.class, () -> new ParentActor(latch, stopLatch, postStopLatch, terminatedLatch));

        return defaultHektor.actorOf(props, "parent");
    }

    public static class LatchContext {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final CountDownLatch stopLatch = new CountDownLatch(1);
        private final CountDownLatch postStopLatch = new CountDownLatch(1);
        private final CountDownLatch terminatedLatch = new CountDownLatch(1);

        public LatchContext() {

        }

        /**
         * When an actor is stopped we expect stop, postStop and then a terminated message
         * to appear so least wait for all of those...
         */
        public void awaitShutdownLatches() throws Exception {
            stopLatch.await();
            postStopLatch.await();
            terminatedLatch.await();
        }

        public Props parentProps() {
            return Props.forActor(ParentActor.class, () -> new ParentActor(latch, stopLatch, postStopLatch, terminatedLatch));
        }
    }


}