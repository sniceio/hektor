package io.hektor.core;

import io.hektor.config.HektorConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Basic tests to configure and start Hektor and send some basic messages through.
 *
 * @author jonas@jonasborjesson.com
 */
public class HektorTest extends HektorTestBase {

    @Test
    public void testBuildHektor() throws Exception {
        final HektorConfiguration config = loadConfig("hektor_config.yaml");
        final Hektor hektor = Hektor.withName("hello").withConfiguration(config).build();
        assertThat(hektor, not((Hektor) null));

        Props props = Props.forActor(DummyActor.class).build();
        final ActorRef ref = hektor.actorOf(props, "hello");
        final ActorRef ref2 = hektor.actorOf(props, "hello2");
        final ActorRef ref3 = hektor.actorOf(props, "hello3");
        assertThat(ref, not((ActorRef) null));
        ref.tell("hello world");
        ref2.tell("hello to the 2nd one");
        ref3.tell("hello to the 3rd one");
        ref3.tell("hello again no 3");
        ref.tell("hello again");
        Thread.sleep(200);
    }


}