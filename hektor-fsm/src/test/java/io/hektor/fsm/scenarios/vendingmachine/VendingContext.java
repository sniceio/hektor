package io.hektor.fsm.scenarios.vendingmachine;

import io.hektor.fsm.Context;
import io.hektor.fsm.Scheduler;

import java.util.List;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class VendingContext implements Context{

    private final CoinReturn coinReturn;

    public VendingContext(final CoinReturn coinReturn) {
        this.coinReturn = coinReturn;
    }

    public void returnCoin(final Coin coin) {
        coinReturn.returnCoin(coin);
    }

    public void returnCoins(final List<Coin> coins) {
        coinReturn.returnCoins(coins);
    }

    @Override
    public Scheduler getScheduler() {
        return null;
    }
}
