package io.hektor.actors.io;

import io.hektor.core.ActorRef;
import io.hektor.core.HektorTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InputStreamActorTest extends HektorTestBase {

    private static final Logger logger = LoggerFactory.getLogger(InputStreamActorTest.class);

    private ExecutorService threadPool;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        threadPool = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearDown() throws Exception {
        threadPool.shutdownNow();
    }


    @Test(timeout = 500)
    public void testSystemIn() throws Exception {
        // use this one to validate the InputStreamActor actually reads it all correctly.
        final String loremIpsum = loadTextFile("lorem_ipsum.txt");

        final InputStream is = loadStream("lorem_ipsum.txt");
        final CountDownLatch latch = new CountDownLatch(1);
        final ActorRef recv = defaultHektor.actorOf("receiver", TokenReceiverActor.props(latch, is, threadPool));

        // the TokenReceiverActor waits for the death event of the InputStreamActor
        // and when receiving that one, it will decrease the latch which then means that
        // we can ask for the final result.
        latch.await();
        final Object result = recv.ask("GiveMeResultPlease", recv).toCompletableFuture().get();
        assertThat(result.toString(), is(loremIpsum));
    }

    public void runManual() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final ActorRef recv = defaultHektor.actorOf(TokenReceiverActor.props(latch, System.in, threadPool), "receiver");
        final ActorRef console = defaultHektor.actorOf(InputStreamActor.props(System.in, threadPool), "console");
        recv.monitor(console);
        Thread.sleep(5000);
        System.in.close();
        latch.await();
        final Object result = recv.ask("GiveMeResultPlease", recv).toCompletableFuture().get();
        System.out.println("You typed: " + result);
        System.exit(1);
    }

    public void runConsole() throws Exception {
        final ActorRef console = defaultHektor.actorOf(ConsoleActor.props(System.in, System.out), "console");
    }

    public static void main(final String... args) throws Exception {
        final InputStreamActorTest testing = new InputStreamActorTest();
        testing.setUp();
        testing.runConsole();
    }

}
