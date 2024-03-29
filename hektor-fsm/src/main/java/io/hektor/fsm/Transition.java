package io.hektor.fsm;

import io.hektor.fsm.docs.Label;
import io.hektor.fsm.visitor.FsmVisitor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This interface represents the transition between two states.
 * When an event is processed by agiven {@link State}, it will
 * match that event against all its {@link Transition}s and if one
 * matches, the FSM will transition fo the new state.
 *
 * Note: in the current model, a {@link Transition} always live in
 * the context of a {@link State} and as such, there is no need to
 * keep track of the from state in this class (which is why only
 * {@link Transition#getToState()} exists)
 *
 * @author jonas@jonasborjesson.com
 */
public interface Transition<E, S extends Enum<S>, C extends Context, D extends Data> {

    /**
     * See if the given event is matching this transition.
     *
     * @param event the event to match agianst this transition.
     * @return true if the event indeed matches, false otherwise.
     */
    boolean match(Object event, C ctx, D data);

    /**
     * This {@link Transition} represents the transition to
     * this particular state. Assuming the event matches of course.
     *
     * @return the to state
     */
    S getToState();

    Optional<Label> getGuardLabel();

    /**
     * Get the associated action wih this transition, if it exists.
     */
    Optional<Consumer<E>> getAction();

    Optional<Action<E, C, D>> getStatefulAction();

    Optional<Label> getActionLabel();

    Class<E> getEventType();
    /**
     * Get the transformation associated with this {@link Transition}. A transformation
     * may only exists if this is a transition out of a transient state.
     *
     * @return the optional transformation for a {@link Transition} out of a transient state.
     */
    Optional<Function<E, ?>> getTransformation();

    Optional<Label> getTransformationLabel();

    void acceptVisitor(FsmVisitor<S, C, D> visitor);
}
