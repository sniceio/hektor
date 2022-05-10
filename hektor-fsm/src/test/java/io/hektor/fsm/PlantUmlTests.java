package io.hektor.fsm;

import io.hektor.fsm.visitor.PlantUmlVisitor;
import org.junit.Before;
import org.junit.Test;

public class PlantUmlTests extends TestBase {

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testGeneratePlantUml() {
        b = builder.withState(SuperSimpleStates.B);
        c = builder.withState(SuperSimpleStates.C);

        a.transitionTo(SuperSimpleStates.B)
                .onEvent(String.class)
                .withGuard("hello"::equals)
                .withAction(PlantUmlTests::processStringForA);

        final var apa = a.transitionTo(SuperSimpleStates.B)
                .onEvent(String.class).withAction(PlantUmlTests::processStringForA);

        b.transitionTo(SuperSimpleStates.C).onEvent(String.class);
        b.transitionTo(SuperSimpleStates.H).onEvent(Integer.class);
        c.transitionTo(SuperSimpleStates.H).onEvent(String.class);
        final var def = builder.build();
        final var visitor = new PlantUmlVisitor();
        visitor.start();
        def.acceptVisitor(visitor);
        visitor.end();
    }

    private static void processStringForA(final String s) {
        System.err.println(s);
    }

}