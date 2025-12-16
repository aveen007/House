package com.medical.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AnalysisStatusConverter implements AttributeConverter<AnalysisStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(AnalysisStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }
    @Override
    public AnalysisStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : AnalysisStatus.fromCode(dbData);
    }
}
