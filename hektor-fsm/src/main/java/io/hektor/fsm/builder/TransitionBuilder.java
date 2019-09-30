package io.hektor.fsm.builder;

import io.hektor.fsm.Action;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Guard;
import io.hektor.fsm.Transition;
import io.hektor.fsm.builder.exceptions.FSMBuilderException;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface TransitionBuilder<E extends Object, S extends Enum<S>, C extends Context, D extends Data> {

    /**
     *
     * @param guard
     * @return
     * @throws IllegalStateException in case you are trying to specify a guard for a transition that
     * has been marked as a default transition (by definition, that "guard" is then accepting all)
     */
    TransitionBuilder<E, S, C, D> withGuard(Predicate<E> guard) throws IllegalStateException;

    /**
     *
     * @param guard
     * @return
     * @throws IllegalStateException in case you are trying to specify a guard for a transition that
     * has been marked as a default transition (by definition, that "guard" is then accepting all)
     */
    TransitionBuilder<E, S, C, D> withGuard(Guard<E, C, D> guard) throws IllegalStateException;

    TransitionBuilder<E, S, C, D> consume();

    TransitionBuilder<E, S, C, D> withAction(Consumer<E> action);

    TransitionBuilder<E, S, C, D> withAction(Action<E, C, D> action);

    /**
     * For transitions to a transient state, you can optionally specify a transformation that will be applied
     * to the original event before entering the state you're automatically transitioning to.
     *
     * NOTE: the validation that the target state is indeed a transient state cannot be performed until the
     * actual state machine definition is built ({@link FSMBuilder#build()}) so if you specify a transformation
     * on a {@link Transition} that go to a "regular" state, then it will not be detected until later at which time
     * an {@link FSMBuilderException} will be thrown.
     *
     * @param transformation
     * @param <R>
     * @return
     * @throws IllegalStateException in case a transformation has already been specified.
     */
    <R> TransitionBuilder<E, S, C, D> withTransformation(Function<E, R> transformation) throws IllegalStateException;

    Transition<E, S, C, D> build();
}
