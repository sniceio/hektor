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

    static class Builder {

        private List<Object> args;

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


        public Props build() throws NoSuchMethodException {
            final Constructor<? extends Actor> constructor = ReflectionHelper.findConstructor(clazz, args);
            return new DefaultProps(clazz, constructor, args);
        }

        private static class DefaultProps implements Props {

            private final Class<? extends Actor> clazz;
            private final Constructor<? extends Actor> constructor;
            private final List<Object> args;

            private DefaultProps(final Class<? extends Actor> clazz,
                                 final Constructor<? extends Actor> constructor,
                                 final List<Object> args) {
                this.clazz = clazz;
                this.constructor = constructor;
                this.args = args;
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

        }

    }


}
