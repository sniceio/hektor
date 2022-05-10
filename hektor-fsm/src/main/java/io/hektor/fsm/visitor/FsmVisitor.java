package io.hektor.fsm.visitor;

import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.State;
import io.hektor.fsm.Transition;

public interface FsmVisitor<S extends Enum<S>, C extends Context, D extends Data> {

    void visit(State<S, C, D> state);

    void visit(S from, Transition<? extends Object, S, C, D> transition);
}
