package io.hektor.actors.fsm;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;

public class DummyFsm {

    public static final Definition<DummyState, DummyContext, DummyData> definition;

    static {
        final var builder = FSM.of(DummyState.class).ofContextType(DummyContext.class)
                .withDataType(DummyData.class)
                .withFriendlyName("dummy");

        final var a = builder.withInitialState(DummyState.A);
        final var d = builder.withFinalState(DummyState.D);

        a.transitionTo(DummyState.A).onEvent(String.class).withGuard("stay"::equals);
        a.transitionTo(DummyState.D).onEvent(String.class).withGuard("exit"::equals).withAction((evt, ctx, data) -> ctx.doExit());

        definition = builder.build();
    }
}
