package io.hektor.fsm.scenarios.vendingmachine;

import io.hektor.fsm.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public class VendingData implements Data {

    /**
     * All our accepted coins.
     */
    private List<Coin> acceptedCoins = new ArrayList<>();

    /**
     * Accept a new coin and return the total value in cents of all coins
     * the user has entered into our vending machine
     * @param coin
     * @return the total value of all coins in cents
     */
    public int acceptCoin(final Coin coin) {
        acceptedCoins.add(coin);
        return totalValueOfAcceptedCoins();
    }

    public int totalValueOfAcceptedCoins() {
        return acceptedCoins.stream().mapToInt(Coin::getValue).sum();
    }

    public List<Coin> returnAllCoins() {
        final List<Coin> coins = acceptedCoins;
        acceptedCoins = new ArrayList<>();
        return coins;
    }
}
