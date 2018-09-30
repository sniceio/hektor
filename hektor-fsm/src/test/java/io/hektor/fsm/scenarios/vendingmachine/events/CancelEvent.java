package io.hektor.fsm.scenarios.vendingmachine.events;

/**
 * Depending on how complex your objects are you may want to create
 * a particular event class like this one or just strings if the events
 * do not carry any extra information.
 *
 * Obviously, the cancel event below could just as well have been a string such
 * as "CANCEL" but personally, I prefer type safety. Easier to refactor, easier to
 * extend and less chance of messing up. E.g., misspelling "CANCEL" somewhere etc.
 *
 * @author jonas@jonasborjesson.com
 */
public class CancelEvent {
}
