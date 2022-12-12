package io.hektor.actors.fsm;

import io.hektor.actors.SubscriptionManagementSupport;
import io.hektor.core.ActorContext;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Props;
import io.hektor.core.Scheduler;
import io.hektor.core.TransactionalActor;
import io.hektor.fsm.Context;
import io.hektor.fsm.Data;
import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;

/**
 * Quite often you want to have your {@link FSM} running within an actor. This provides the
 * execution environment for your FSM, default handling of logging etc.
 *
 */
public final class FsmActor<S extends Enum<S>, C extends Context, D extends Data> extends SubscriptionManagementSupport implements TransactionalActor, Logging {

    private static final Logger logger = LoggerFactory.getLogger(FsmActor.class);

    private final Definition<S, C, D> definition;

    private FSM<S, C, D> fsm;

    private ActorPath myPath;

    private final Function<ActorRef, C> contextSupplier;
    private final Supplier<D> dataSupplier;

    private C context;
    private D data;

    private final OnStartFunction<C, D> onStart;
    private final OnStopFunction<C, D> onStop;

    private io.snice.logging.Context logCtx;



    public static <S extends Enum<S>, C extends Context, D extends Data> Builder<S, C, D> of(final Definition<S, C, D> definition) {
        assertNotNull(definition, "The FSM definition cannot be null");
        return new Builder(definition);
    }

    private FsmActor(final Definition<S, C, D> definition,
                     final Function<ActorRef, C> context,
                     final Supplier<D> data,
                     final OnStartFunction<C, D> onStart,
                     final OnStopFunction<C, D> onStop) {
        super(false);
        this.definition = definition;
        this.contextSupplier = context;
        this.dataSupplier = data;
        this.onStart = onStart;
        this.onStop = onStop;
    }

    @Override
    public void start() {
        myPath = ctx().self().path();

        logCtx = visitor -> visitor.accept("actor", myPath.name());

        logInfo(logCtx, "Starting");
        // TODO: if these throw exception, we need to deal with and kill the actor.
        context = contextSupplier.apply(self());
        data = dataSupplier.get();
        onStart.start(ctx(), context, data);

        fsm = definition.newInstance(myPath, context, data, this::unhandledEvent, null);

        invokeFsm((ignore) -> fsm.start(), null);
    }

    @Override
    public void stop() {
        logInfo(logCtx, "Stopping");
        onStop.stop(ctx(), context, data);
    }

    @Override
    public void postStop() {
        logInfo(logCtx, "Stopped");
    }

    @Override
    protected void onEvent(final Object msg) {
        invokeFsm((o) -> fsm.onEvent(o), msg);
    }

    public void unhandledEvent(final S state, final Object o) {
        // left empty intentionally. The FSM will alert on warning. Nothing we can do here.
    }

    private void invokeFsm(final Consumer<Object> exec, final Object msg) {
        final var ctx = ctx();
        final var ctxAdaptor = new FsmActorContextAdaptorSupport(ctx);
        final var schedulerAdaptor = new FsmSchedulerAdaptor(ctx().scheduler(), self());
        Context._scheduler.set(schedulerAdaptor);
        FsmActorContextSupport._ctx.set(ctxAdaptor); // only matters if the Context actually is implementing this support interface
        try {
            exec.accept(msg);
            if (fsm.isTerminated()) {
                ctx().stop();
            }
        } finally {
            FsmActorContextSupport._ctx.remove();
            Context._scheduler.remove();
            ctxAdaptor.processMessages(this);
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public static class Builder<S extends Enum<S>, C extends Context, D extends Data> {

        private final Definition<S, C, D> definition;

        private Function<ActorRef, C> context;
        private Supplier<D> data;

        private OnStartFunction<C, D> onStart;
        private OnStopFunction<C, D> onStop;

        private Builder(final Definition<S, C, D> definition) {
            this.definition = definition;
        }

        public Builder withContext(final C context) {
            assertNotNull(context, "The context cannot be null");
            this.context = (ref) -> context;
            return this;
        }

        public Builder withContext(final Supplier<C> context) {
            assertNotNull(context, "The context supplier cannot be null");
            this.context = (ref) -> context.get();
            return this;
        }

        public Builder withContext(final Function<ActorRef, C> context) {
            assertNotNull(context, "The context function cannot be null");
            this.context = context;
            return this;
        }


        public Builder withData(final D data) {
            assertNotNull(data, "The data cannot be null");
            this.data = () -> data;
            return this;
        }

        public Builder withData(final Supplier<D> data) {
            assertNotNull(data, "The data supplier cannot be null");
            this.data = data;
            return this;
        }

        public Builder withStartFunction(final OnStartFunction<C, D> onStart) {
            assertNotNull(onStart, "The on start function cannot be null");
            this.onStart = onStart;
            return this;
        }

        public Builder withStopFunction(final OnStopFunction<C, D> onStop) {
            assertNotNull(onStop, "The on stop function cannot be null");
            this.onStop = onStop;
            return this;
        }

        public Props build() {
            ensureNotNull(context, "You must supply the Context");
            ensureNotNull(data, "You must supply the Data");
            return Props.forActor(FsmActor.class, () ->
                    new FsmActor(definition, context, data, ensureOnStart(), ensureOnStop()));
        }

        private OnStartFunction<C, D> ensureOnStart() {
            if (onStart != null) {
                return onStart;
            }
            return (actorCtx, ctx, data) -> {};
        }

        private OnStopFunction<C, D> ensureOnStop() {
            if (onStop != null) {
                return onStop;
            }
            return (actorCtx, ctx, data) -> {};
        }

    }

    private class FsmActorContextAdaptorSupport implements FsmActorContextAdaptor {

        private final ActorContext ctx;
        private Object msg;
        private List<Object> messages;

        private FsmActorContextAdaptorSupport(final ActorContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public ActorRef self() {
            return ctx.self();
        }

        @Override
        public void stop() {
            ctx.stop();
        }

        @Override
        public Optional<ActorRef> lookup(final String path) {
            return ctx.lookup(path);
        }

        @Override
        public Optional<ActorRef> lookup(final ActorPath path) {
            return ctx.lookup(path);
        }

        @Override
        public Scheduler scheduler() {
            return ctx.scheduler();
        }

        @Override
        public Optional<ActorRef> child(final String child) {
            return ctx.child(child);
        }

        @Override
        public void stash() {
            ctx.stash();
        }

        @Override
        public void unstash() {
            ctx.unstash();
        }

        @Override
        public ActorRef actorOf(final String name, final Props props) {
            return ctx.actorOf(name, props);
        }

        @Override
        public ActorRef sender() {
            return ctx.sender();
        }

        @Override
        public void tellSubscribers(final Object msg) {
            if (this.msg == null) {
                this.msg = msg;
            } else {
                ensureMessageList().add(msg);
            }
        }

        private List<Object> ensureMessageList() {
            if (messages == null) {
                messages = new ArrayList<>(5);
            }
            return messages;
        }

        /**
         * Process all the buffered "tell subscriber" messages.
         */
        private void processMessages(final FsmActor actor) {
            if (msg == null) {
                return;
            }

            actor.tellSubscribers(msg);
            msg = null;
            if (messages != null) {
                messages.forEach(actor::tellSubscribers);
                messages = null;
            }
        }
    }
}
