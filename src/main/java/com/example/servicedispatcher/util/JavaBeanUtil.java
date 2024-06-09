package com.example.servicedispatcher.util;

import java.lang.reflect.Field;
import java.util.Map;

public class JavaBeanUtil {

    /**
     * 将map转为java对象
     *
     * map中的key和java对象的属性名相同时，赋值。
     * 私有属性也能赋值，通过反射爆破直接给属性赋值。
     *
     * @param map 数据源
     * @param clazz 目标对象的类型
     * @param includeSuperClass 是否给父类的属性赋值。true，给父类的属性赋值；false，只给当前类的属性赋值。
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz, boolean includeSuperClass) throws InstantiationException, IllegalAccessException {
        T obj = clazz.newInstance();
        if (map == null || map.isEmpty()) {
            return obj;
        }
        Class<?> currentClass = clazz;
        boolean allowLoop = true;
        while (currentClass != null && currentClass != Object.class && allowLoop) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                Object value = map.get(fieldName);
                if (value != null && field.getType().isAssignableFrom(value.getClass())) {
                    field.setAccessible(true);
                    field.set(obj, value);
                }
            }
            currentClass = currentClass.getSuperclass();
            allowLoop = includeSuperClass;
        }
        return obj;
    }

}
