package io.hektor.fsm.builder.impl;

import io.hektor.fsm.Action;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Guard;
import io.hektor.fsm.Transition;
import io.hektor.fsm.builder.TransitionBuilder;
import io.hektor.fsm.builder.exceptions.ActionAlreadyDefinedException;
import io.hektor.fsm.builder.exceptions.GuardAlreadyDefinedException;
import io.hektor.fsm.impl.TransitionImpl;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.snice.preconditions.PreConditions.ensureNotNull;


/**
 * @author jonas@jonasborjesson.com
 */
public class TransitionBuilderImpl<E extends Object, S extends Enum<S>, C extends Context, D extends Data> implements TransitionBuilder<E, S, C, D> {

    private final S toState;
    private final Class<E> event;

    /**
     * whether or not this is a default transition.
     */
    private final boolean isDefault;

    /**
     * Whether or not this transition is out of a transient state.
     */
    private final boolean isTransient;

    private Predicate<E> guard;
    private Guard<E, C, D> richerGuard;

    private Consumer<E> action;
    private Action<E, C, D> statefulAction;

    /**
     * An optional transformation that will transform the event into the return value,
     * which then is the new value that will be given to the target state.
     */
    private Function<E, ?> transformation;

    public TransitionBuilderImpl(final S toState, final Class<E> event) {
        this(toState, event, false, false);
    }

    public TransitionBuilderImpl(final S toState, final Class<E> event, final boolean isDefault) {
        this(toState, event, isDefault, false);
    }

    public TransitionBuilderImpl(final S toState, final Class<E> event, final boolean isDefault, final boolean isTransient) {
        this.toState = toState;
        this.event = event;
        this.isDefault = isDefault;
        this.isTransient = isTransient;
    }

    @Override public TransitionBuilder<E, S, C, D> withGuard(final Predicate<E> guard) {
        ensureGuardIsVisible();
        assertGuard();
        this.guard = guard;
        return this;
    }

    @Override public TransitionBuilder<E, S, C, D> withGuard(final Guard<E, C, D> guard) {
        ensureGuardIsVisible();
        assertGuard();
        this.richerGuard = guard;
        return this;
    }

    /**
     * An empty action will be installed that will just silently consume the event.
     *
     */
    @Override
    public TransitionBuilder<E, S, C, D> consume() {
        return withAction(e -> {});
    }

    @Override
    public TransitionBuilder<E, S, C, D> withAction(final Consumer<E> action) {
        assertAction();
        this.action = action;
        return this;
    }

    @Override
    public TransitionBuilder<E, S, C, D> withAction(final Action<E, C, D> action) {
        assertAction();
        this.statefulAction = action;
        return this;
    }

    @Override
    public <R> TransitionBuilder<E, S, C, D> withTransformation(final Function<E, R> transformation) throws IllegalStateException {
        ensureNotNull(transformation, "The transformation function cannot be null");
        if (this.transformation != null) {
            throw new IllegalStateException("A transformation has already been given, you cannot specify it twice.");
        }
        // TODO: actually we can't have this one here because we do not know if the state we are transitioning
        // to is a transient one at this point in time.
        // if (!isTransient) {
            // throw new IllegalStateException("You cannot specify a transformation on a transition that isn't a transition from a transient state");
        // }

        this.transformation = transformation;
        return this;
    }

    @Override
    public Transition<E, S, C, D> build() {
        return new TransitionImpl(null, toState, event, guard, richerGuard, action, statefulAction, transformation);
    }

    private void assertGuard() throws GuardAlreadyDefinedException {
        if (this.guard != null || this.richerGuard != null) {
            throw new GuardAlreadyDefinedException("A guard has already been defined on this transition");
        }
    }
    private void assertAction() throws ActionAlreadyDefinedException {
        if (this.action != null || statefulAction != null) {
            throw new ActionAlreadyDefinedException("An action has already been defined on this transition");
        }
    }

    private void ensureGuardIsVisible() {
        if (isDefault) {
            throw new IllegalStateException("You cannot use a guard with the default transition");
        }
    }

}
