package es.oamonitor.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

@Converter(autoApply = true)
public class BinaryUuidConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String s) {
        return s == null ? null : UUID.fromString(s);
    }
}
