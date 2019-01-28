package org.kie.cloud.integrationtests.optaweb.rest;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;

@Provider
public class OptaWebObjectMapperResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    public OptaWebObjectMapperResolver() {
        objectMapper = new ObjectMapper()
                .registerModule(OptaPlannerJacksonModule.createModule())
                .registerModule(new JavaTimeModule())
                // Write OffsetDateTime's (and similar) to JSON in ISO-8601 format,
                // for example: 2007-12-03T10:15:30+01:00
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

}