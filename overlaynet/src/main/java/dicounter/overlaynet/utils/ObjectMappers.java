package dicounter.overlaynet.utils;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import dicounter.overlaynet.exception.JsonException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMappers {

    private static ObjectMapper objectMapper = createObjectMapper();

    public static String writeValueAsString(@NonNull final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            throw Exceptions.logError(new JsonException("Caught an exception while serializing an object to json. Object: " + object, e));
        }
    }

    public static <T> T readValue(@NonNull final String value, @NonNull final Class<T> valueType) {
        try {
            return objectMapper.readValue(value, valueType);
        } catch (final IOException e) {
            throw Exceptions.logError(new JsonException("Caught an exception while deserializing a value: " + value, e));
        }
    }

    public static <T> T readValue(@NonNull final String value, @NonNull final TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (final IOException e) {
            throw Exceptions.logError(new JsonException("Caught an exception while deserializing a value: " + value, e));
        }
    }

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(UUID.class, new UUIDDeserializer());

        mapper.registerModule(simpleModule);
        mapper.registerModule(new JodaModule());

        mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(dateFormat);

        return mapper;
    }
}