package com.hayan.Account.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter(autoApply = true)
@RequiredArgsConstructor
public class EnumConverter<E extends Enum<E>> implements AttributeConverter<E, String> {
    private final Class<E> enumClass;

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return dbData != null ? Enum.valueOf(enumClass, dbData) : null;
    }
}