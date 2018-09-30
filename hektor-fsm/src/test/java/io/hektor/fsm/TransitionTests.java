package io.hektor.fsm;

import io.hektor.fsm.builder.TransitionBuilder;
import io.hektor.fsm.impl.TransitionImpl;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.hektor.fsm.SimpleFsmStates.DONE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author jonas@jonasborjesson.com
 */
public class TransitionTests {

    @Test
    public void testBasicEventMatching() {
        final Transition<String, SimpleFsmStates, Context, Data> transition =
                new TransitionImpl<>("Testing transition", DONE, String.class, null, null, null, null);

        assertThat(transition.getToState(), is(DONE));

        assertThat(transition.match("hello world"), is(true));

        assertThat(transition.match(12), is(false));
        assertThat(transition.match(new Object()), is(false));
    }

    /**
     * Ensure that two guards cannot be specified on a transition.
     */
    @Test
    public void testThatTwoGuardsCannotBeSpecified() {
        final TransitionBuilder<String, SimpleFsmStates, Context, Data> builder = new TransitionBuilder<>(DONE, String.class);
        builder.withGuard(s -> s.equals("hello"));
        assertGuardRejected(builder, s -> s.equals("here we go again")); // should be rejected
        assertGuardRejected(builder, (str, ctx, data) -> true);
    }

    /**
     * Ensure that two actions cannot be specified on a transition.
     */
    @Test
    public void testThatTwoActionsCannotBeSpecified() {
        final TransitionBuilder<String, SimpleFsmStates, Context, Data> builder = new TransitionBuilder<>(DONE, String.class);
        builder.withAction(System.out::println); // the single Consumer<String> action
        assertActionRejected(builder, System.out::println); // should be rejected
        assertActionRejected(builder, (str, ctx, data) -> System.out.println("not allowed either"));
    }

    private void assertActionRejected(final TransitionBuilder<String, SimpleFsmStates, Context, Data> builder,
                                      final Consumer<String> action) {
        try {
            builder.withAction(action); // not allowed
            fail("We were not supposed to be able to register a second action");
        } catch (final Exception e)  {
            // expected
        }
    }

    private void assertActionRejected(final TransitionBuilder<String, SimpleFsmStates, Context, Data> builder,
                                      final Action<String, Context, Data> action) {
        try {
            builder.withAction(action); // not allowed
            fail("We were not supposed to be able to register a second action");
        } catch (final Exception e)  {
            // expected
        }
    }

    private void assertGuardRejected(final TransitionBuilder<String, SimpleFsmStates, Context, Data> builder,
                                      final Predicate<String> guard) {
        try {
            builder.withGuard(guard); // not allowed
            fail("We were not supposed to be able to register a second guard");
        } catch (final Exception e)  {
            // expected
        }
    }

    private void assertGuardRejected(final TransitionBuilder<String, SimpleFsmStates, Context, Data> builder,
                                      final Guard<String, Context, Data> guard) {
        try {
            builder.withGuard(guard); // not allowed
            fail("We were not supposed to be able to register a second guard");
        } catch (final Exception e)  {
            // expected
        }
    }


    @Test
    public void testMatchWithGuard() {
        final Predicate<String> guard = s -> s.equals("Hello World");

        final Transition<String, SimpleFsmStates, Context, Data> transition =
                new TransitionImpl<>("Testing transition",  DONE, String.class, guard, null, null, null);

        assertThat(transition.match("Hello World"), is(true));

        // wrong case
        assertThat(transition.match("Hello world"), is(false));
    }

}
