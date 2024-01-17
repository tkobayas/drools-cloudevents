package org.drools.cloudevents;

import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import org.kie.api.prototype.PrototypeFactInstance;

import static org.kie.api.prototype.PrototypeBuilder.prototype;

public class JsonUtil {

    private static final ObjectMapper DEFAULT_MAPPER = createMapper(new JsonFactory());

    private static final TypeReference<Map<String, Object>> MAP_OF_STRING_AND_OBJECT = new TypeReference<>(){};

    private static ObjectMapper createMapper(JsonFactory jsonFactory) {
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        SimpleModule module = new SimpleModule();
        mapper.registerModule(module);
        return mapper;
    }

    public static String objectToString(Object data) {
        try {
            return DEFAULT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static CloudEvent createEvent(Object data) {
        var dataEvent = PojoCloudEventData.wrap(data, DEFAULT_MAPPER::writeValueAsBytes);

        return CloudEventBuilder.v1()
                .withSource(URI.create("example"))
                .withType("fact." + data.getClass().getSimpleName())
                .withId(UUID.randomUUID().toString())
                .withDataContentType(MediaType.APPLICATION_JSON)
                .withData(dataEvent)
                .build();
    }

    public static Map<String, Object> readValueAsMapOfStringAndObject(String json) {
        try {
            return DEFAULT_MAPPER.readValue(json, MAP_OF_STRING_AND_OBJECT);
        } catch (JacksonException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static PrototypeFactInstance cloudEventToPrototypeFact(CloudEvent cloudEvent) {
        Map<String, Object> map = readValueAsMapOfStringAndObject(new String(cloudEvent.getData().toBytes()));
        PrototypeFactInstance fact = prototype(cloudEvent.getType()).asFact().newInstance();
        map.forEach(fact::put);
        return fact;
    }
}
