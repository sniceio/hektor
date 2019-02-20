package io.hektor.fsm.builder;

import io.hektor.fsm.Action;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Transition;

import java.util.function.Consumer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DefaultTransitionBuilder<E extends Object, S extends Enum<S>, C extends Context, D extends Data> {

    /**
     * An empty action will be installed that will just silently consume the event.
     *
     */
    DefaultTransitionBuilder<E, S, C, D> consume();

    DefaultTransitionBuilder<E, S, C, D> withAction(final Consumer<E> action);

    DefaultTransitionBuilder<E, S, C, D> withAction(final Action<E, C, D> action);

    Transition<E, S, C, D> build();
}
