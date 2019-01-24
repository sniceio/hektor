package io.hektor.actors.io;

import io.hektor.core.Actor;
import io.hektor.core.ActorRef;

/**
 * An actor whose purpose it is to read from an input stream and emit
 * tokens and/or lines to a given {@link ActorRef}.
 *
 * Use this actor for dealing with blocking I/O in a non-blocking Actor kind of way.
 */
public class InputStreamActor implements Actor {

    @Override
    public void onReceive(Object msg) {

    }
}
