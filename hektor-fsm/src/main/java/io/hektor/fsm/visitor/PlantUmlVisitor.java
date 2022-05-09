package io.hektor.fsm.visitor;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;

public class PlantUmlVisitor<S extends Enum<S>, C extends Context, D extends Data> implements FsmVisitor<S, C, D> {

    public void start() {
        System.out.println("@startuml");
    }

    public void end() {
        System.out.println("@enduml");
    }


    @Override
    public void visit(final State<S, C, D> state) {
        final var sb = new StringBuilder();
        sb.append("state ").append(state.getState()).append(": ");
        System.out.println(sb);
    }

    @Override
    public void visit(final S from, final Transition<?, S, C, D> transition) {
        final var event = transition.getEventType().getSimpleName();
        System.out.println(from + " --> " + transition.getToState() + ": " + event);
    }
}
