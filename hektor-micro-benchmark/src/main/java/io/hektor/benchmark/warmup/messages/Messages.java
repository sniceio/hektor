package io.hektor.benchmark.warmup.messages;

/**
 * @author jonas@jonasborjesson.com
 */
public class Messages {

    /**
     * Simple message asking the receiving actor to use ctx.stop() to
     * terminate itself.
     */
    public static class TerminateYourself { }

    /**
     * Message to ask the actor to create a child.
     *
     * @author jonas@jonasborjesson.com
     */
    public static class CreateChild {
        public final String child;

        public CreateChild(final String child) {
            this.child = child;
        }
    }

    /**
     * Simple message containing a string and that's it.
     *
     * @author jonas@jonasborjesson.com
     */
    public static class SimpleMessage {

        public final String msg;

        public SimpleMessage(final String msg) {
            this.msg = msg;
        }
    }
}
