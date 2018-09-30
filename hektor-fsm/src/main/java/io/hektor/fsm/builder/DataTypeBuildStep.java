package io.hektor.fsm.builder;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DataTypeBuildStep<S extends Enum<S>, C extends Context> {

    <D extends Data> FSMBuilder<S, C, D> withDataType(final Class<D> type);
}
