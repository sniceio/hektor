package io.hektor.fsm.visitor;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;

import java.util.ArrayList;
import java.util.stream.Collectors;

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

        final var labels = new ArrayList<String>();
        state.getInitialEnterActionLabel().ifPresent(l -> labels.add("onInitialEnter: " + l));
        state.getEnterActionLabel().ifPresent(l -> labels.add("onEnter: " + l));
        state.getSelfEnterActionLabel().ifPresent(l -> labels.add("onSelfEnter: " + l));
        state.getExitActionLabel().ifPresent(l -> labels.add("onExit: " + l));
        sb.append(labels.stream().collect(Collectors.joining("\\n")));

        System.out.println(sb);
    }

    @Override
    public void visit(final S from, final Transition<?, S, C, D> transition) {
        final var sb = new StringBuilder();
        final var event = transition.getEventType().getSimpleName();
        sb.append(from).append(" --> ").append(transition.getToState());
        sb.append(": ").append(event);

        final var labels = new ArrayList<String>();
        transition.getGuardLabel().ifPresent(l -> labels.add("[" + l + "]"));
        transition.getActionLabel().ifPresent(l -> labels.add("/" + l));
        transition.getTransformationLabel().ifPresent(l -> labels.add("transform[" + l + "]"));
        sb.append(labels.stream().collect(Collectors.joining("\\n")));
        System.out.println(sb);
    }
}
