package io.hektor.fsm.scenarios.vendingmachine;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.hektor.fsm.builder.FSMBuilder;
import io.hektor.fsm.builder.StateBuilder;
import io.hektor.fsm.scenarios.vendingmachine.events.CancelEvent;

import static io.hektor.fsm.scenarios.vendingmachine.VendingMachineFSM.VendingState.IDLE;
import static io.hektor.fsm.scenarios.vendingmachine.VendingMachineFSM.VendingState.INSERTING_COINS;
import static io.hektor.fsm.scenarios.vendingmachine.VendingMachineFSM.VendingState.OFF;

/**
 * Imagine a simple coffee machine. A user can insert coins, once there is enough
 * money entered, the user can choose which coffee to make (for simplicity all coffee
 * cost the same) and if all goes well, a cup will be delivered.
 *
 * Also, the user can choose to cancel the operation up until the user has asked the
 * vending machine to actually start brewing.
 *
 *               +----------+
 *               |  off     |<--------------+   +------------------------------+
 *               +----------+   power down  |   |       cancel                 |
 *                                          |   |                              |
 *                                          |   v                              |
 *     +-----------+                     +---------+                     +-------------+
 *     |  service  |    reset button     |  idle   |  coin inserted      |   inserting | <----+
 *     |  needed   +-------------------> |         +-------------------> |   coins     |      |
 *     +-----------+                     +---------+                     +-------------+      |
 *           ^                             ^     ^   cancel                       |  |        |
 *           |                     done    |     |                                |  +--------+
 *           |                   +---------+     +-------------+                  |    not enough money
 *           |                   |                             |                  |
 *           |         +------------+                     +-----------+           |
 *           |         |    make    |<--------------------+  user     |<----------+
 *           +---------+    coffee  |    button pushed    |  choose   |    enough or more money than price
 *         error       +------------+                     +-----------+
 *
 *
 * @author jonas@jonasborjesson.com
 */
public class VendingMachineFSM {

    /**
     * These are the states we can be in, according to our state machine defined above.
     */
    enum VendingState {
        OFF, SERVICE_NEEDED, IDLE, INSERTING_COINS, MAKE_COFFEE, USER_CHOOSE;
    }

    /**
     * This is the definition of our FSM and should implement the state machine
     * defined above.
     */
    public final static Definition<VendingState, VendingContext, VendingData> definition;

    static {
        // let's start off by defining all states, which is the initial state, which one is
        // the final state and if there are any entry/exit actions.
        // Note: if we were documenting the FSM in a real tool, as opposed to ascii, we would
        //       also specify the actions but it got too messy so I left it out.

        // 1. Create the main builder. We will define all possible states (by passing in the enum type)
        //    our state machine can take. We will also let the FSM framework know the type of
        //    Context and Data we will be using.
        final FSMBuilder<VendingState, VendingContext, VendingData> builder =
                FSM.of(VendingState.class).ofContextType(VendingContext.class).withDataType(VendingData.class);

        // 2. Define all the states:
        final StateBuilder<VendingState, VendingContext, VendingData> idle = builder.withInitialState(IDLE);
        final StateBuilder<VendingState, VendingContext, VendingData> insertingCoins = builder.withState(INSERTING_COINS);
        // final StateBuilderImpl<VendingState, VendingContext, VendingData> userChoose = builder.withState(USER_CHOOSE);
        // final StateBuilderImpl<VendingState, VendingContext, VendingData> makeCoffee = builder.withState(MAKE_COFFEE);
        // final StateBuilderImpl<VendingState, VendingContext, VendingData> serviceNeeded = builder.withState(SERVICE_NEEDED);
        final StateBuilder<VendingState, VendingContext, VendingData> off = builder.withFinalState(OFF);

        // 3. Define all enter/exit actions.
        //    Of course, you can define them in the same statement when you define them above.
        //    However, this is an example and even though readable code is always important, it
        //    is even more important in examples like this one.
        off.withEnterAction(VendingMachineFSM::onEnterOff);


        // 4. Define all the transitions between the states and any potential actions to take on those
        //    transitions.

        // Check if the coin is valid and if it is, then we transition over to the INSERTING_COINS state.
        // Note: it is important to understand that the order in which the transitions are created
        // actually matters. They are simply added to an internal list and tried in that order and
        // the first one that matches will be executed and the rest ignored. The two transitions for
        // Coin below starts off with the more specific one (it has the guard) and if that one doesn't
        // match, then we know that it cannot be a valid coin so then we will simply stay in the same state.
        idle.transitionTo(INSERTING_COINS).onEvent(Coin.class).withGuard(VendingMachineFSM::validateCoin).withAction(VendingMachineFSM::acceptCoin);
        idle.transitionTo(IDLE).onEvent(Coin.class).withAction(VendingMachineFSM::rejectCoin);

        // Keep accepting coins until we have reached the correct amount for a cup of coffee
        // or until the user hits the cancel button.
        insertingCoins.transitionTo(INSERTING_COINS).onEvent(Coin.class).withGuard(VendingMachineFSM::validateCoin).withAction(VendingMachineFSM::acceptCoin);
        insertingCoins.transitionTo(INSERTING_COINS).onEvent(Coin.class).withAction(VendingMachineFSM::rejectCoin);
        insertingCoins.transitionTo(IDLE).onEvent(CancelEvent.class).withAction(VendingMachineFSM::cancel);
        definition = builder.build();
    }

    private static void cancel(final CancelEvent cancel, final VendingContext ctx, final VendingData data) {
        ctx.returnCoins(data.returnAllCoins());
    }

    private static void acceptCoin(final Coin coin, final VendingContext ctx, final VendingData data) {
        final int totalValue = data.acceptCoin(coin);
        System.err.println("Accepting coin of value " + coin.getValue() + " and now the user has entered " + totalValue + " cents");
    }

    private static void rejectCoin(final Coin coin, final VendingContext ctx, final VendingData data) {
        ctx.returnCoin(coin);
    }

    /**
     * In our implementation, we are not actually going to do anything per se but if this
     * was a real vending machine we would have to actually check so that the coin matches
     * known coins. Since this is just an example, I just wanted to show that you can have
     * a guard that would be checked as well in order to match an event.
     *
     * @param coin
     * @return
     */
    private static boolean validateCoin(final Coin coin) {
        return coin != Coin.UNKNOWN;
    }

    /**
     * When the machine power down, perhaps we want to do some clean up. Compare with the tasks
     * when shutting down a computer.
     *
     * @param ctx
     * @param data
     */
    private static void onEnterOff(final VendingContext ctx, final VendingData data) {
        // do some clean-up.
    }
}
