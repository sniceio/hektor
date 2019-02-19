package io.hektor.fsm.builder;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;

/**
 * @author jonas@jonasborjesson.com
 */
public interface EventBuildStep<S extends Enum<S>, C extends Context, D extends Data> {

    <E> TransitionBuilder<E, S, C, D> onEvent(Class<E> event);

    /**
     *
     * @param <E>
     * @return
     */
    <E> DefaultTransitionBuilder<E, S, C, D> asDefaultTransition();
}
