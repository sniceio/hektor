package io.hektor.fsm.builder;

import io.hektor.fsm.Context;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ContextTypeBuildStep<S extends Enum<S>> {

    <C extends Context> DataTypeBuildStep<S, C> ofContextType(Class<C> type);

    // <D extends Data> FSMBuilder<S, C, D> ofDataType(Class<D> type);
}
