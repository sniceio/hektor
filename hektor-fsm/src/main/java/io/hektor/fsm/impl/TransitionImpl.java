package io.hektor.fsm.impl;

import io.hektor.fsm.Action;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Guard;
import io.hektor.fsm.Transition;
import io.hektor.fsm.docs.Label;
import io.hektor.fsm.visitor.FsmVisitor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Optional.empty;

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
    private final Optional<Label> guardLabel;

    private final Optional<Consumer<E>> action;
    private final Optional<Action<E, C, D>> statefulAction;
    private final Optional<Label> actionLabel;

    private final Optional<Function<E, ?>> transformation;
    private final Optional<Label> transformationLabel;

    public TransitionImpl(final String description,
                          final S to,
                          final Class<E> event,
                          final Predicate<E> guard,
                          final Guard<E, C, D> richerGuard,
                          final Optional<Label> guardLabel,
                          final Consumer<E> action,
                          final Action<E, C, D> statefulAction,
                          final Optional<Label> actionLabel,
                          final Function<E, ?> transformation,
                          final Optional<Label> transformationLabel) {
        this.description = description;
        this.to = to;
        this.event = event;
        this.guard = guard;
        this.richerGuard = richerGuard;
        this.guardLabel = guardLabel;
        this.action = Optional.ofNullable(action);
        this.statefulAction = Optional.ofNullable(statefulAction);
        this.actionLabel = actionLabel;
        this.transformation = Optional.ofNullable(transformation);
        this.transformationLabel = transformationLabel;
    }

    public TransitionImpl(final String description,
                          final S to,
                          final Class<E> event,
                          final Predicate<E> guard,
                          final Guard<E, C, D> richerGuard,
                          final Consumer<E> action,
                          final Action<E, C, D> statefulAction,
                          final Function<E, ?> transformation) {
        this(description, to, event, guard, richerGuard, empty(), action, statefulAction, empty(), transformation, empty());
    }

    @Override
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
    public Optional<Label> getGuardLabel() {
        return guardLabel;
    }


    @Override
    public Optional<Consumer<E>> getAction() {
        return action;
    }

    @Override
    public Optional<Action<E, C, D>> getStatefulAction() {
        return statefulAction;
    }

    @Override
    public Optional<Label> getActionLabel() {
        return actionLabel;
    }

    @Override
    public Class<E> getEventType() {
        return event;
    }

    @Override
    public Optional<Function<E, ?>> getTransformation() {
        return transformation;
    }

    @Override
    public Optional<Label> getTransformationLabel() {
        return transformationLabel;
    }

    @Override
    public void acceptVisitor(final FsmVisitor<S, C, D> visitor) {
        visitor.visit(null, this);
    }
}
