package util;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public JacksonObjectMapperProvider() {

        mapper = ObjectMapperConfigurator.configureJackson();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}