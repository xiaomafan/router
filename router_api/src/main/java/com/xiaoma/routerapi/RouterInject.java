package com.xiaoma.routerapi;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class RouterInject {

    private static final Map<Class<?>, Constructor<? extends ParametersInject>> INJECT_MAP = new LinkedHashMap();

    public static void inject(Context target) {
        createInjecting(target);
    }

    private static ParametersInject createInjecting(Context target) {
        Constructor<? extends ParametersInject> constructor = findInjectorForClass(target.getClass());
        if (constructor == null) {
            return ParametersInject.EMPYT;
        }
        try {
            return constructor.newInstance(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to invoke " + constructor, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to invoke " + constructor, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException("Unable to create injecting instance.", cause);
        }
    }

    private static Constructor<? extends ParametersInject> findInjectorForClass(Class<?> targetClass) {

        Constructor<? extends ParametersInject> constructor = INJECT_MAP.get(targetClass);
        if (constructor != null) {
            return constructor;
        }
        String className = targetClass.getName();
        try {
            Class<?> injectClass = Class.forName(className + "_RouterInjecting");
            constructor = (Constructor<? extends ParametersInject>) injectClass.getConstructor(targetClass);
        } catch (NoSuchMethodException e) {
            constructor = findInjectorForClass(targetClass.getSuperclass());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        INJECT_MAP.put(targetClass, constructor);
        return constructor;
    }
}
