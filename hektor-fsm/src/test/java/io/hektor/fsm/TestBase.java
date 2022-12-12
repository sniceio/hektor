package io.hektor.fsm;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import io.hektor.fsm.builder.FSMBuilder;
import io.hektor.fsm.builder.StateBuilder;
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestBase {

    private static final Logger logger = LoggerFactory.getLogger(TestBase.class);

    protected FSM<SuperSimpleStates, Context, Data> fsm;
    protected FSMBuilder<SuperSimpleStates, Context, Data> builder;
    protected StateBuilder<SuperSimpleStates, Context, Data> a;
    protected StateBuilder<SuperSimpleStates, Context, Data> b;
    protected StateBuilder<SuperSimpleStates, Context, Data> c;
    protected StateBuilder<SuperSimpleStates, Context, Data> d;
    protected StateBuilder<SuperSimpleStates, Context, Data> e;
    protected StateBuilder<SuperSimpleStates, Context, Data> f;
    protected StateBuilder<SuperSimpleStates, Context, Data> g;
    protected StateBuilder<SuperSimpleStates, Context, Data> h;

    @Before
    public void setUp() {
        final var ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        final var root = ctx.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        root.detachAndStopAllAppenders();
        configureJsonAppender(ctx, root);

        builder = FSM.of(SuperSimpleStates.class).ofContextType(Context.class).withDataType(Data.class);

        // for simplicity sake, all our unit tests will have the initial state set as A and
        // the final state as H.
        a = builder.withInitialState(SuperSimpleStates.A);
        h = builder.withFinalState(SuperSimpleStates.H);

    }

    /**
     * Helper method to setup json logging. Not actually used for testing but just nice if you also want to
     * view the full mdc etc while figuring out a particular test.
     */
    private static void configureJsonAppender(final LoggerContext ctx, final ch.qos.logback.classic.Logger root) {
        final var appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(ctx);

        final var encoder = new LoggingEventCompositeJsonEncoder();
        encoder.setContext(ctx);
        final var provider = new LoggingEventJsonProviders();
        provider.addLogLevel(new LogLevelJsonProvider());
        provider.addMessage(new MessageJsonProvider());
        provider.addMdc(new MdcJsonProvider());
        encoder.setProviders(provider);
        encoder.start();
        appender.setEncoder(encoder);

        appender.start();
        root.addAppender(appender);
    }

    public enum SuperSimpleStates {
        A, B, C, D, E, F, G, H;
    }

    public void go(final Object event) {
        fsm = build();
        fsm.start();
        fsm.onEvent(event);
    }

    public FSM<SuperSimpleStates, Context, Data> build() {
        return builder.build().newInstance("uuid-123", mock(Context.class), mock(Data.class),
                TestBase::onUnhandledEvent, TestBase::onTransition);
    }

    private static void onUnhandledEvent(final SuperSimpleStates state, final Object event) {
        fail("I did not expect a unhandled event");
    }

    private static void onTransition(final SuperSimpleStates from, final SuperSimpleStates to, final Object event) {
    }


}
