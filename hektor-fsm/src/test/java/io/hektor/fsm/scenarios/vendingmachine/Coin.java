package io.hektor.fsm.scenarios.vendingmachine;

/**
 * @author jonas@jonasborjesson.com
 */
public enum Coin {
    PENNY(1, "Lincoln Penny"),
    NICKEL(5, "Jefferson Nickel"),
    DIME(10, "10-cent"),
    QUARTER(25, "Quarter Dollar"),
    HALF_DOLLAR(50, "Kennedy Half Dollar"),
    DOLLAR(100, "Dollar Coin"), // apparently many of them...
    UNKNOWN(0, "Unknown"); // apparently many of them...

    private final int value;
    private final String friendlyName;

    Coin(final int value, final String friendlyName) {
        this.value = value;
        this.friendlyName = friendlyName;
    }

    public int getValue() {
        return value;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public String toString() {
        return friendlyName;
    }

}
