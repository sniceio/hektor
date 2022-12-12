package io.hektor.actors.fsm;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import io.hektor.config.HektorConfigurator;
import io.hektor.core.Hektor;
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActorFsmTestBase {

    private Hektor hektor;

    @Before
    public void setup() {
        final var ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        final var root = ctx.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        root.detachAndStopAllAppenders();
        configureJsonAppender(ctx, root);

        hektor = HektorConfigurator.defaultHektor("UnitTest");

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

}
