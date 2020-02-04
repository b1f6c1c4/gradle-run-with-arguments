package com.github.b1f6c1c4.gradle_run_with_arguments;

class Reflection {
    public static Object Get(Object obj, String... fields) throws IllegalAccessException {
        for (var f : fields)
            obj = Get(obj, f);
        return obj;
    }

    public static Object Get(Object obj, String field) throws IllegalAccessException {
        var c = obj.getClass();
        while (true) {
            try {
                var f = c.getDeclaredField(field);
                f.setAccessible(true);
                return f.get(obj);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
    }

    public static void Set(Object obj, Object val, String... fields) throws IllegalAccessException {
        for (var i = 0; i < fields.length - 1; i++)
            obj = Get(obj, fields[i]);
        Set(obj, fields[fields.length - 1], val);
    }

    public static void Set(Object obj, String field, Object val) throws IllegalAccessException {
        var c = obj.getClass();
        while (true) {
            try {
                var f = c.getDeclaredField(field);
                f.setAccessible(true);
                f.set(obj, val);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
    }
}
