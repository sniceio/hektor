package io.hektor.fsm.builder;

import io.hektor.fsm.Action;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Guard;
import io.hektor.fsm.Transition;
import io.hektor.fsm.impl.TransitionImpl;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author jonas@jonasborjesson.com
 */
public class TransitionBuilder<E extends Object, S extends Enum<S>, C extends Context, D extends Data> {

    private final S toState;
    private final Class<E> event;
    private Predicate<E> guard;
    private Guard<E, C, D> richerGuard;

    private Consumer<E> action;
    private Action<E, C, D> statefulAction;

    public TransitionBuilder(final S toState, final Class<E> event) {
        this.toState = toState;
        this.event = event;
    }

    public TransitionBuilder<E, S, C, D> withGuard(final Predicate<E> guard) {
        assertGuard();
        this.guard = guard;
        return this;
    }

    public TransitionBuilder<E, S, C, D> withGuard(final Guard<E, C, D> guard) {
        assertGuard();
        this.richerGuard = guard;
        return this;
    }

    /**
     * An empty action will be installed that will just silently consume the event.
     *
     */
    public TransitionBuilder<E, S, C, D> consume() {
        return withAction(e -> {});
    }

    public TransitionBuilder<E, S, C, D> withAction(final Consumer<E> action) {
        assertAction();
        this.action = action;
        return this;
    }

    public TransitionBuilder<E, S, C, D> withAction(final Action<E, C, D> action) {
        assertAction();
        this.statefulAction = action;
        return this;
    }

    public Transition<E, S, C, D> build() {
        return new TransitionImpl(null, toState, event, guard, richerGuard, action, statefulAction);
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
}
