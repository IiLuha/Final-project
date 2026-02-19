package com.itdev.finalproject.dto.kafka;

public record FieldChange<T>(
        T oldValue,
        T newValue,
        String fieldName,
        Class<T> fieldType
) {
}
