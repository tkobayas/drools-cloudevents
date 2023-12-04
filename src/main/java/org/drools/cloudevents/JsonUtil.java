package org.drools.cloudevents;

import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import org.drools.base.facttemplates.Fact;
import org.drools.model.PrototypeFact;
import org.drools.model.PrototypeFactFactory;

import static org.drools.model.PrototypeDSL.prototype;

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
                .withType(data.getClass().getCanonicalName())
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

    public static PrototypeFact cloudEventToPrototypeFact(CloudEvent cloudEvent) {
        Map<String, Object> map = readValueAsMapOfStringAndObject(new String(cloudEvent.getData().toBytes()));
        return PrototypeFactFactory.get().createMapBasedFact(prototype(cloudEvent.getType()), map);
    }
}
