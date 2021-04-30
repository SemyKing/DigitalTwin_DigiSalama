package com.example.demo.utils;

import com.example.demo.database.models.utils.ValidationResponse;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldReflectionUtils<T>{

    public FieldReflectionUtils() {}


    public T getEntityWithEmptyStringValuesAsNull(T entity) {

        List<Field> stringFields = new ArrayList<>();

        Field[] allFields = entity.getClass().getDeclaredFields();
        for (Field field : allFields) {
            if (field.getType().equals(String.class)) {
                stringFields.add(field);
            }
        }

        for (Field field : stringFields) {
            field.setAccessible(true);
            Object fieldParameter = ReflectionUtils.getField(field, entity);

            if (fieldParameter instanceof String) {
                if (((String) fieldParameter).length() <= 0) {
                    field.setAccessible(true);
                    ReflectionUtils.setField(field, entity, null);
                }
            }
        }

        return entity;
    }

    public ValidationResponse validateStringFields(T entity) {

        List<Field> stringFields = new ArrayList<>();

        Field[] allFields = entity.getClass().getDeclaredFields();
        for (Field field : allFields) {
            if (field.getType().equals(String.class)) {
                stringFields.add(field);
            }
        }

        for (Field field : stringFields) {
            field.setAccessible(true);
            Object fieldParameter = ReflectionUtils.getField(field, entity);

            if (fieldParameter != null) {
                if (fieldParameter instanceof String) {
                    if (((String) fieldParameter).length() <= 0) {
                        return new ValidationResponse(false, "'" + field.getName() + "' cannot be empty");
                    }
                }
            }
        }

        return new ValidationResponse(true, "validation successful");
    }
}
