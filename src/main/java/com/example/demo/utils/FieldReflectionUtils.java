package com.example.demo.utils;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldReflectionUtils<T>{

    public FieldReflectionUtils() {
    }

    public FieldReflectionUtils(T object) {

    }

    public T getObjectWithEmptyStringValuesAsNull(T object) {

        List<Field> stringFields = new ArrayList<>();

        Field[] allFields = object.getClass().getDeclaredFields();
        for (Field field : allFields) {
            if (field.getType().equals(String.class)) {
                stringFields.add(field);
            }
        }

        for (Field field : stringFields) {
            field.setAccessible(true);
            Object fieldParameter = ReflectionUtils.getField(field, object);

            if (fieldParameter instanceof String) {
                if (((String) fieldParameter).length() <= 0) {
                    field.setAccessible(true);
                    ReflectionUtils.setField(field, object, null);
                }
            }
        }

        return object;
    }
}
