package io.hektor.core;

import io.hektor.core.internal.ReflectionHelper;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 */
public class PropsTest {

    @Test
    public void testBuildEmptyConstructor() throws Exception {
        final Props props = Props.forActor(TestDummyActor.class).build();
        assertConstructorExists(props);
        assertTestDummyActor(props);
    }

    @Test
    public void testBuilConstructorSingleArg() throws Exception {
        Props props = Props.forActor(TestDummyActor.class).withConstructorArg(33).build();
        assertConstructorExists(props);
        assertTestDummyActor(props, 33);

        props = Props.forActor(TestDummyActor.class).withConstructorArg("hello world").build();
        assertConstructorExists(props);
    }

    @Test
    public void testBuildActorWithTwoArgs() throws Exception {
        Props props = Props.forActor(TestDummyActor.class)
                .withConstructorArg("hello world")
                .withConstructorArg(23)
                .build();
        assertConstructorExists(props);
        assertTestDummyActor(props, "hello world", 23);
    }

    @Test
    public void testBuildConstructorThreeArgs() throws Exception {
        List<String> strings = new ArrayList<>();
        strings.add("hello");
        final Object obj = new Object();
        Props props = Props.forActor(TestDummyActor.class)
                .withConstructorArg(23.45d)
                .withConstructorArg(obj)
                .withConstructorArg(strings)
                .build();
        assertConstructorExists(props);
        assertTestDummyActor(props, "the other constructor", 99, 23.45d, obj, strings);
    }

    private void assertConstructorExists(final Props props) {
        assertThat(props.constructor(), not((Constructor<? extends Actor>) null));
    }

    /**
     * Helper method for asserting the dummy actor when we used the empty constructor
     *
     * @param props
     */
    private void assertTestDummyActor(final Props props) {
        assertTestDummyActor(props, "empty constructor", 1, 1.0d, null, Collections.emptyList());
    }

    /**
     * Helper method for asserting the dummy actor when we used the int constructor
     *
     * @param props
     */
    private void assertTestDummyActor(final Props props,
                                      final int expectedInt) {
        assertTestDummyActor(props, "int constructor", expectedInt, 1.0d, null, Collections.emptyList());
    }

    /**
     * Helper method for asserting the dummy actor when we used the String & int constructor
     *
     * @param props
     */
    private void assertTestDummyActor(final Props props,
                                      final String expectedString,
                                      final int expectedInt) {
        assertTestDummyActor(props, expectedString, expectedInt, 1.0d, null, Collections.emptyList());
    }

    /**
     * Helper method for asserting all values within the dummy actor.
     */
    private void assertTestDummyActor(final Props props,
                                      final String expectedString,
                                      final int expectedInt,
                                      final Double expectedDouble,
                                      final Object expectedObject,
                                      final List<String> expectedStringList) {
        final TestDummyActor actor = (TestDummyActor)ReflectionHelper.constructActor(props);
        assertThat(actor.someValue, is(expectedInt));
    }


    public static class TestDummyActor implements Actor {

        public final String hello;
        public final int someValue;
        public final Double whatever;
        public final Object object;
        public final List<String> strings;

        public TestDummyActor() {
            this("empty constructor");
        }

        public TestDummyActor(final String hello) {
            this(hello, 1);
        }

        public TestDummyActor(final Integer value) {
            this("int constructor", value);
        }

        public TestDummyActor(final String hello, final Integer value) {
            this.hello = hello;
            this.someValue = value;
            whatever = 1.0d;
            object = null;
            strings = Collections.emptyList();
        }

        public TestDummyActor(final Double whatever, final Object object, final List<String> strings) {
            this.whatever = whatever;
            this.object = object;
            this.strings = strings;
            hello = "the other constructor";
            someValue = 99;
        }

        public TestDummyActor(final Object object, final Double whatever, final List<String> strings) {
            this.whatever = whatever;
            this.object = object;
            this.strings = strings;
            hello = "the other opposite constructor";
            someValue = 98;
        }

        @Override
        public void onReceive(final ActorContext cttx, final Object msg) {

        }
    }

}