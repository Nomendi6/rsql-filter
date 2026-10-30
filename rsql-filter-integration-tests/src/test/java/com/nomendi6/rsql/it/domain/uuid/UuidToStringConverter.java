package com.nomendi6.rsql.it.domain.uuid;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/** Like the reporter's UUIDToStringConverter: a UUID kept as its 36-character text, lower case. */
@Converter
public class UuidToStringConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        return dbData == null ? null : UUID.fromString(dbData);
    }
}
