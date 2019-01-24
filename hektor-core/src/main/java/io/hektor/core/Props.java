package io.hektor.core;

import com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2;
import io.hektor.core.internal.PreConditions;

import java.util.Optional;
import java.util.function.Supplier;

import static io.hektor.core.internal.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Props<T extends Actor> {

    static <T extends Actor> CreatorStep<T> forActor(final Class<T> clazz) {
        return creator -> {
            assertNotNull(creator, "The creator cannot be null");
            return new Builder<T>(clazz, creator);
        };
    }

    /**
     * Create a new {@link Props} for the given {@link Actor}.
     *
     * @param clazz the actual class of the implementing {@link Actor}
     * @param creator a factory function for creating the actual actor instance.
     * @return
     */
    static <T extends Actor> Props<T> forActor(final Class<T> clazz, final Supplier<T> creator) {
        assertNotNull(clazz, "The class cannot be null");
        assertNotNull(creator, "The creator cannot be null");
        return new Builder.DefaultProps<>(clazz, creator, null);
    }

    Class<T> clazz();

    /**
     * The function for creating a new instance of the actor.
     *
     * @return
     */
    Supplier<T> creator();

    Optional<Router> router();

    interface CreatorStep<T extends Actor> {
        Builder<T> withCreator(Supplier<T> creator);
    }

    class Builder<T extends Actor> {

        // private List<Object> args;

        private Router router;

        private final Class<T> clazz;
        private final Supplier<T> creator;

        private Builder(final Class<T> clazz, final Supplier<T> creator) {
            this.clazz = clazz;
            this.creator = creator;
        }

        public Builder withRouter(final Router router) {
            this.router = router;
            return this;
        }

        public Props build() throws NoSuchConstructorException {
            return new DefaultProps(clazz, creator, router);
        }

        private static class DefaultProps<T extends Actor> implements Props<T> {

            private final Class<T> clazz;
            private final Supplier<T> creator;
            private final Optional<Router> router;

            private DefaultProps(final Class<T> clazz,
                                 final Supplier<T> creator,
                                 final Router router) {
                this.clazz = clazz;
                this.creator = creator;
                this.router = Optional.ofNullable(router);
            }

            @Override
            public Class<T> clazz() {
                return clazz;
            }

            @Override
            public Supplier<T> creator() {
                return creator;
            }

            @Override
            public Optional<Router> router() {
                return router;
            }
        }
    }
}
