package io.hektor.fsm.impl;

import io.hektor.fsm.Action;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Guard;
import io.hektor.fsm.Transition;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author jonas@jonasborjesson.com
 */
public class TransitionImpl<E, S extends Enum<S>, C extends Context, D extends Data> implements Transition<E, S, C, D> {

    /**
     * This is a human readable description of this transition.
     * E.g. "Transition to terminated if we receive the quit command".
     * It is only used for logging.
     */
    private final String description;

    private final S to;
    private final Class<E> event;

    /**
     * An optional guard can be put in place if we wish to be more specific
     * than just match against the actual event type.
     */
    private final Predicate<E> guard;
    private final Guard<E, C, D> richerGuard;

    private final Optional<Consumer<E>> action;
    private final Optional<Action<E, C, D>> statefulAction;

    public TransitionImpl(final String description,
                          final S to,
                          final Class<E> event,
                          final Predicate<E> guard,
                          final Guard<E, C, D> richerGuard,
                          final Consumer<E> action,
                          final Action<E, C, D> statefulAction) {
        this.description = description;
        this.to = to;
        this.event = event;
        this.guard = guard;
        this.richerGuard = richerGuard;
        this.action = Optional.ofNullable(action);
        this.statefulAction = Optional.ofNullable(statefulAction);
    }

    public boolean match(final Object event, final C ctx, final D data) {
        if (!this.event.isAssignableFrom(event.getClass())) {
            return false;
        }

        if (guard != null) {
            return guard.test((E)event);
        }

        if (richerGuard != null) {
            return richerGuard.test((E)event, ctx, data);
        }

        return true;
    }

    @Override
    public S getToState() {
        return to;
    }

    @Override
    public Optional<Consumer<E>> getAction() {
        return action;
    }

    @Override
    public Optional<Action<E, C, D>> getStatefulAction() {
        return statefulAction;
    }
}
