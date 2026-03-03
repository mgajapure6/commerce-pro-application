package com.commerce_pro_backend.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Converter
@Slf4j
public class CustomFieldsConverter implements AttributeConverter<Map<String, String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, String>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting custom fields to JSON", e);
            throw new RuntimeException("Failed to serialize custom fields", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, TYPE_REF);
        } catch (JsonProcessingException e) {
            log.error("Error parsing custom fields JSON: {}", dbData, e);
            return new HashMap<>();
        }
    }
}