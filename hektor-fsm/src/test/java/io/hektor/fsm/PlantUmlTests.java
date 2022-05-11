package io.hektor.fsm;

import io.hektor.fsm.visitor.PlantUmlVisitor;
import org.junit.Before;
import org.junit.Test;

import static io.hektor.fsm.docs.Label.label;

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

        b.withSelfEnterAction((ctx, data) -> System.out.println("Hello to me"), label("Talking to self"));
        b.withInitialEnterAction((ctx, data) -> System.out.println("First Time"), label("Prepare"));
        b.withEnterAction((ctx, data) -> System.out.println("Entering"), label("Reset"));
        b.withExitAction((ctx, data) -> System.out.println("Bye"), label("Collect Data"));

        a.transitionTo(SuperSimpleStates.B)
                .onEvent(String.class)
                .withGuard("hello"::equals, label("s == hello"))
                .withAction(PlantUmlTests::countChars, label("Count chars"));

        a.transitionTo(SuperSimpleStates.B).onEvent(String.class).withAction(PlantUmlTests::countChars);

        b.transitionTo(SuperSimpleStates.C).onEvent(String.class);
        b.transitionTo(SuperSimpleStates.H).onEvent(Integer.class).withGuard(i -> i < 10, label("<10"));
        b.transitionTo(SuperSimpleStates.B).onEvent(Integer.class).withGuard(i -> i >= 10, label(">=10"));
        c.transitionTo(SuperSimpleStates.H).onEvent(String.class);
        final var def = builder.build();
        final var visitor = new PlantUmlVisitor();
        visitor.start();
        def.acceptVisitor(visitor);
        visitor.end();
    }

    private static void countChars(final String s) {
        System.err.println(s.length());
    }

}