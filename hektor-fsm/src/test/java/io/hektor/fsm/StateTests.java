package io.hektor.fsm;

import io.hektor.fsm.builder.StateBuilder;
import io.hektor.fsm.builder.StateBuilderException;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static io.hektor.fsm.SimpleFsmStates.DEAD;
import static io.hektor.fsm.SimpleFsmStates.INIT;
import static io.hektor.fsm.SimpleFsmStates.WORKING;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author jonas@jonasborjesson.com
 */
public class StateTests {

    @Test
    public void testBuildState() {
        final StateBuilder<SimpleFsmStates, Context, Data> builder = new StateBuilder(INIT);

        final CountDownLatch helloLatch = new CountDownLatch(1);
        final CountDownLatch quitLatch = new CountDownLatch(1);
        builder.transitionTo(WORKING).onEvent(String.class).withGuard(s -> s.equals("Hello")).withAction(s -> helloLatch.countDown());
        builder.transitionTo(DEAD).onEvent(String.class).withGuard(s -> s.equals("QUIT")).withAction(s -> quitLatch.countDown());

        final State init = builder.build();
        assertThat(init.getState(), is(INIT));

        assertThat(init.accept("doesnt match").isPresent(), is(false));
        assertThat(init.accept(new Object()).isPresent(), is(false));
        assertThat(init.accept(helloLatch).isPresent(), is(false));

        acceptAndExecuteAction(init, "Hello");
        assertThat(helloLatch.getCount(), is(0L));

        acceptAndExecuteAction(init, "QUIT");
        assertThat(quitLatch.getCount(), is(0L));
    }

    /**
     * You must specify transitions for non-final states.
     */
    @Test(expected = StateBuilderException.class)
    public void testInitialStateMissingTransitions() {
        new StateBuilder(INIT).isInital(true).build();
    }

    /**
     * You must specify transitions for non-final states.
     */
    @Test(expected = StateBuilderException.class)
    public void testMissingTransitions() {
        new StateBuilder(INIT).build();
    }

    /**
     * On the other hand, final states CANNOT have transitions.
     */
    @Test(expected = StateBuilderException.class)
    public void testFinalStatehasTransitions() {
        final StateBuilder<SimpleFsmStates, Context, Data> builder = new StateBuilder(DEAD).isFinal(true);
        builder.transitionTo(INIT).onEvent(String.class);
        builder.build();
    }

    /**
     * Helper method for executing the action associated with a transition. This helper method
     * assumes that the event indeed is matching and then there has been an action defined.
     * You will blow up if none of the above is true.
     */
    private void acceptAndExecuteAction(final State state, final Object event) {
        final Transition<Object, SimpleFsmStates, Context, Data> transition = ((Transition<Object, SimpleFsmStates, Context, Data>)state.accept(event).get());
        transition.getAction().get().accept(event);
    }
}
