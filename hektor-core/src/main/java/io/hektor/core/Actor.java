package io.hektor.core;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public interface Actor {

    ThreadLocal<ActorContext> _ctx = new ThreadLocal<>();

    default ActorRef self() {
        return _ctx.get().self();
    }

    default ActorRef sender() {
        return _ctx.get().sender();
    }

    default ActorContext ctx() {
        return _ctx.get();
    }

    /**
     * Will be invoked whenever this Actor receives a message.
     *
     * Note that any messages emitted by this Actor as a result of this invocation
     * will not be processed until this method returns. Hence, the "hello world"
     * message, as shown below, will actually not be sent back to the sender
     * until this method returns cleanly.
     *
     * <pre>
     *     ...
     *     context.sender().tell("hello world", context.self());
     *     ...
     * </pre>
     *
     * Also note that any exceptions thrown by this method will be
     * handled by the actor system and the supervisor of this actor
     * will be informed. This actor is considered dead but may be revived
     * if the supervisor so wishes. Any potential messages that should have
     * been sent to other actors or
     * any other events of any type will NOT be handled due to the exception.
     * Hence, the "It's a cruel world" message, as shown in the snippet below, will
     * actually never ever be sent back to the sender due to the exception escaping
     * this method.
     *
     * <pre>
     *     ...
     *     context.sender().tell("It's a cruel world", context.self());
     *     ...
     *     throw new RuntimeException("I crashed!!!");
     * </pre>
     *
     * @param context
     * @param msg
     */
    void onReceive(ActorContext context, Object msg);
}
