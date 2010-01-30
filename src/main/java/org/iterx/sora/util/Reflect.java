package org.iterx.sora.util;

import java.lang.Exception;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import static org.iterx.sora.util.Exception.rethrow;

public final class Reflect {

    private Reflect() {
    }

    public static <T> Constructor<T> findConstructor(final Class<T> cls,
                                                     final Class... parameterTypes) {

        try {
            return cls.getConstructor(parameterTypes);
        }
        catch(final NoSuchMethodException e) {
        }
        catch(final Exception e) {
            throw rethrow(e);
        }
        return null;
    }

    public static Method findMethod(final Object object,
                                    final String name,
                                    final Class returnType,
                                    final Class... parameterTypes) {
        try {
            final Method method = object.getClass().getMethod(name, parameterTypes);
            if(method.getReturnType().equals(returnType)) return method;
        }
        catch(final NoSuchMethodException e) {
        }
        catch(final Exception e) {
            throw rethrow(e);
        }
        return null;
    }

    public static <T> T invokeConstructor(final Constructor<T> constructor,
                                          final Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        }
        catch(final Exception e) {
            throw rethrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(final Method method,
                                     final Object instance,
                                     final Object... arguments) {
        try {
            return (T) method.invoke(instance, arguments);
        }
        catch(final Exception e) {
            throw rethrow(e);
        }
    }
}
