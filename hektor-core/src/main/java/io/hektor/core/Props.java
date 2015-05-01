package io.hektor.core;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Props {

    static Builder forActor(Class<? extends Actor> clazz) {
        return new Builder(clazz);
    }

    Class<? extends Actor> clazz();

    static class Builder {

        private final Class<? extends Actor> clazz;

        private Builder(Class<? extends Actor> clazz) {
            this.clazz = clazz;
        }

        public Props build() {
            return new DefaultProps(clazz);
        }

        private static class DefaultProps implements Props {

            private final Class<? extends Actor> clazz;

            private DefaultProps(Class<? extends Actor> clazz) {
                this.clazz = clazz;
            }

            @Override
            public Class<? extends Actor> clazz() {
                return clazz;
            }
        }

    }


}
