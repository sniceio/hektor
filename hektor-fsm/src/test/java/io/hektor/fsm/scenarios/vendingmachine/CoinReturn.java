package io.hektor.fsm.scenarios.vendingmachine;

import java.util.List;

/**
 * We are mimicking a vending machine and they do have a coin return slot
 * and this interface represents that so we can "return" change or all
 * the coins if the user cancels the operation.
 *
 */
public interface CoinReturn {
    void returnCoin(Coin coin);
    void returnCoins(List<Coin> coins);
}
