package io.hektor.fsm.scenarios.vendingmachine;

import io.hektor.fsm.FSM;
import io.hektor.fsm.TransitionListener;
import io.hektor.fsm.scenarios.vendingmachine.VendingMachineFSM.VendingState;
import io.hektor.fsm.scenarios.vendingmachine.events.CancelEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit test and also serves as an example for how to actually
 * test Hektor FSMs.
 */
public class VendingMachineTest {

    private FSM vendingMachine;
    private VendingContext ctx;
    private VendingData data;
    private final CoinReturn coinReturn = Mockito.mock(CoinReturn.class);

    @Before
    public void setUp() {
        ctx = new VendingContext(coinReturn);
        data = new VendingData();
        vendingMachine = VendingMachineFSM.definition.newInstance("some-uuid-123", ctx, data);
        vendingMachine.start();
    }

    /**
     * Whenever there is a un-handled event, i.e., an event for which we
     * have not defined a transition, the "un-handled event listener" will be
     * called, if you have one configured. It is just a simple {@link BiConsumer}
     * and you can register one when you create the FSM. Ensure that this works.
     */
    @Test
    public void testUnhandledEvent() {
        final String unmatchedEvent = "won't match anything";
        final BiConsumer<VendingState, Object> unhandledEventHandler = mock(BiConsumer.class);

        vendingMachine = VendingMachineFSM.definition.newInstance("some-uuid-123", ctx, data, unhandledEventHandler, null);
        vendingMachine.start();

        // insert a coin and we should now be in the
        // "inserting coin" state and here we will send in
        // the event that won't match and our handler should
        // be called.
        vendingMachine.onEvent(Coin.QUARTER);
        vendingMachine.onEvent(unmatchedEvent);
        verify(unhandledEventHandler).accept(VendingState.INSERTING_COINS, unmatchedEvent);
    }

    /**
     * You can also register a listener for state transitions. Ensure
     * that this listener (or lambda rather) is actually invoked.
     * <p>
     * Note: this listener is invoked just before the actual
     * transition is executed.
     */
    @Test
    public void testStateTransitionListener() {

        final TransitionListener<VendingState> listener = mock(TransitionListener.class);
        vendingMachine = VendingMachineFSM.definition.newInstance("some-uuid-123", ctx, data, null, listener);
        vendingMachine.start();
        vendingMachine.onEvent(Coin.DIME);

        // So, we inserted a quarter and we did so in the IDLE state and that event should
        // have taken us to the INSERTING_COINS state so our listener should have been
        // called with the following values:
        verify(listener).onTransition(VendingState.IDLE, VendingState.INSERTING_COINS, Coin.DIME);
    }

    /**
     * In this simple test we will just insert a wrong coin
     * and ensure that the vending machine spits it back out.
     */
    @Test
    public void testInsertWrongCoin() {
        vendingMachine.onEvent(Coin.UNKNOWN);
        vendingMachine.onEvent(Coin.QUARTER);
        vendingMachine.onEvent(Coin.UNKNOWN);
        vendingMachine.onEvent(Coin.DIME);
        verify(coinReturn, times(2)).returnCoin(Coin.UNKNOWN);
    }

    /**
     * A user can cancel the order and should then be getting back all
     * the coins she just put into the machine.
     */
    @Test
    public void testCancelOrder() {
        vendingMachine.onEvent(Coin.QUARTER);
        vendingMachine.onEvent(Coin.DIME);
        vendingMachine.onEvent(Coin.QUARTER);
        vendingMachine.onEvent(new CancelEvent());
        verify(coinReturn).returnCoins(Arrays.asList(Coin.QUARTER, Coin.DIME, Coin.QUARTER));
    }
}
