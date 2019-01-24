package io.hektor.core.internal;

import io.hektor.core.Actor;
import io.hektor.core.Props;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public class ReflectionHelper {

    public static Actor constructActor(final Props props) {
        return (Actor)props.creator().get();
        /*
        try {
            final Constructor<? extends Actor> constructor = props.constructor();
            final List<Object> args = props.arguments();
            if (args == null) {
                return constructor.newInstance();
            }
            return constructor.newInstance(args.toArray());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
        */
    }

    public static Constructor<? extends Actor> findConstructor(final Class<? extends Actor> clazz,
                                                               final List<Object> args) throws NoSuchMethodException {
        Constructor<? extends Actor> constructor = null;
        try {
            if (args == null) {
                constructor = clazz.getConstructor();
            } else if (args.size() == 1) {
                constructor = clazz.getConstructor(args.get(0).getClass());
            } else if (args.size() == 2) {
                constructor = clazz.getConstructor(args.get(0).getClass(), args.get(1).getClass());
            } else {
                final Class<?> types[] = new Class<?>[args.size()];
                for(int i = 0; i < args.size(); ++i) {
                    types[i] = args.get(i).getClass();
                }
                constructor = clazz.getConstructor(types);
            }

        } catch (final NoSuchMethodException e) {
            constructor = thoroughConstructorSearch(clazz, args);
        }

        return constructor;
    }

    /**
     *
     * @return
     * @throws NoSuchMethodException
     */
    private static Constructor<? extends Actor> thoroughConstructorSearch(final Class<? extends Actor> clazz,
                                                                          final List<Object> args)
            throws NoSuchMethodException {
        final int count = args.size();
        final Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == count) {
                final Class<?> types[] = constructor.getParameterTypes();
                for (int i = 0; i < count; ++i) {
                    if (types[i].isAssignableFrom(args.get(i).getClass())) {
                        if (i == count - 1) {
                            return (Constructor<? extends Actor>)constructor;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        throw new NoSuchMethodException("apa");
    }

}
