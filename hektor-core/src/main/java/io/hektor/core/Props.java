package io.hektor.core;

import io.hektor.core.internal.ReflectionHelper;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Props {

    static Builder forActor(final Class<? extends Actor> clazz) {
        return new Builder(clazz);
    }

    Class<? extends Actor> clazz();

    Constructor<? extends Actor> constructor();

    List<Object> arguments();

    Router router();

    static class Builder {

        private List<Object> args;

        private Router router;

        private final Class<? extends Actor> clazz;

        private Builder(Class<? extends Actor> clazz) {
            this.clazz = clazz;
        }

        public Builder withConstructorArg(final Object arg) {
            if (args == null) {
                args = new ArrayList<>(3);
            }
            args.add(arg);
            return this;
        }

        public Builder withRouter(final Router router) {
            this.router = router;
            return this;
        }

        public Props build() throws NoSuchMethodException {
            final Constructor<? extends Actor> constructor = ReflectionHelper.findConstructor(clazz, args);
            return new DefaultProps(clazz, constructor, args, router);
        }

        private static class DefaultProps implements Props {

            private final Class<? extends Actor> clazz;
            private final Constructor<? extends Actor> constructor;
            private final List<Object> args;
            private final Router router;

            private DefaultProps(final Class<? extends Actor> clazz,
                                 final Constructor<? extends Actor> constructor,
                                 final List<Object> args,
                                 final Router router) {
                this.clazz = clazz;
                this.constructor = constructor;
                this.args = args;
                this.router = router;
            }

            @Override
            public Class<? extends Actor> clazz() {
                return clazz;
            }

            @Override
            public Constructor<? extends Actor> constructor() {
                return constructor;
            }

            @Override
            public List<Object> arguments() {
                return args;
            }

            @Override
            public Router router() {
                return null;
            }

        }

    }


}
