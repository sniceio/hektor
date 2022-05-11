package io.hektor.fsm.builder;

import io.hektor.fsm.Action;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Guard;
import io.hektor.fsm.Transition;
import io.hektor.fsm.builder.exceptions.FSMBuilderException;
import io.hektor.fsm.docs.Label;

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
    TransitionBuilder<E, S, C, D> withGuard(Predicate<E> guard, Label label) throws IllegalStateException;

    default TransitionBuilder<E, S, C, D> withGuard(final Predicate<E> guard) throws IllegalStateException {
        return withGuard(guard, null);
    }

    /**
     *
     * @param guard
     * @return
     * @throws IllegalStateException in case you are trying to specify a guard for a transition that
     * has been marked as a default transition (by definition, that "guard" is then accepting all)
     */
    TransitionBuilder<E, S, C, D> withGuard(Guard<E, C, D> guard, Label label) throws IllegalStateException;

    default TransitionBuilder<E, S, C, D> withGuard(final Guard<E, C, D> guard) throws IllegalStateException {
        return withGuard(guard, null);
    }

    default TransitionBuilder<E, S, C, D> consume() {
        return withAction(e -> {});
    }

    TransitionBuilder<E, S, C, D> withAction(Consumer<E> action, Label label);

    default TransitionBuilder<E, S, C, D> withAction(final Consumer<E> action) {
        return withAction(action, null);
    }

    TransitionBuilder<E, S, C, D> withAction(Action<E, C, D> action, Label label);

    default TransitionBuilder<E, S, C, D> withAction(final Action<E, C, D> action) {
        return withAction(action, null);
    }

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
    <R> TransitionBuilder<E, S, C, D> withTransformation(Function<E, R> transformation, Label label) throws IllegalStateException;

    default <R> TransitionBuilder<E, S, C, D> withTransformation(final Function<E, R> transformation) throws IllegalStateException {
        return withTransformation(transformation, null);
    }

    Transition<E, S, C, D> build();
}
