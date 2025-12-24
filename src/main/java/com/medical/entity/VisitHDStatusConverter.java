package com.medical.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)

public class VisitHDStatusConverter  implements AttributeConverter<VisitHDStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(VisitHDStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    @Override
    public VisitHDStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : VisitHDStatus.fromCode(dbData);
    }
}
