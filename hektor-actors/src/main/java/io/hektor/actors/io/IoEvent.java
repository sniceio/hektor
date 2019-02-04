package io.hektor.actors.io;

import io.snice.buffer.Buffer;

/**
 *
 */
public interface IoEvent {

    static IoReadEvent readEvent(final Buffer buffer) {
        return IoReadEvent.of(buffer);
    }

    static IoWriteEvent writeEvent(final Buffer buffer) {
        return IoWriteEvent.of(buffer);
    }

    static IoWriteEvent writeEvent(final String str) {
        return IoWriteEvent.of(str);
    }

    default boolean isWriteEvent() {
        return false;
    }

    default IoWriteEvent toWriteEvent() {
        throw new ClassCastException("Unable to cast " + getClass().getName() + " into a " + IoWriteEvent.class.getName());
    }

    default IoReadEvent toReadEvent() {
        throw new ClassCastException("Unable to cast " + getClass().getName() + " into a " + IoReadEvent.class.getName());
    }

    default boolean isReadEvent() {
        return false;
    }

}
