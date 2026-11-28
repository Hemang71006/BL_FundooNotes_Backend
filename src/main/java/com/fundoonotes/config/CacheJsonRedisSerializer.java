package com.fundoonotes.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fundoonotes.dto.response.NoteResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;
import java.util.Map;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Serializes cached DTOs using a versioned JSON envelope with
 *                explicit, allowlisted target types instead of polymorphic class names.
 * Author       : Hemang
 * Date         : 28-11-2026
 */
public class CacheJsonRedisSerializer implements RedisSerializer<Object> {

    private static final Logger logger =
            LoggerFactory.getLogger(CacheJsonRedisSerializer.class);

    private static final String VERSION = "2";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_VALUE = "value";

    private static final String TYPE_STRING = "string";
    private static final String TYPE_NOTE = "noteResponse";
    private static final String TYPE_NOTE_LIST = "noteResponseList";

    private final ObjectMapper objectMapper;
    private final Map<String, JavaType> supportedTypes;

    public CacheJsonRedisSerializer(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;

        JavaType noteResponseType = objectMapper.constructType(NoteResponse.class);
        JavaType noteResponseListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, NoteResponse.class);

        this.supportedTypes = Map.of(
                TYPE_STRING, objectMapper.constructType(String.class),
                TYPE_NOTE, noteResponseType,
                TYPE_NOTE_LIST, noteResponseListType
        );
    }

    @Override
    public byte[] serialize(Object value) throws SerializationException {

        if (value == null) {
            return new byte[0];
        }

        String type = resolveType(value);

        try {
            JsonNode payload = objectMapper.valueToTree(value);

            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put(FIELD_VERSION, VERSION);
            envelope.put(FIELD_TYPE, type);
            envelope.set(FIELD_VALUE, payload);

            return objectMapper.writeValueAsBytes(envelope);
        } catch (Exception exception) {
            throw new SerializationException(
                    "Could not write Redis cache value", exception);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {

        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            JsonNode envelope = objectMapper.readTree(bytes);

            if (envelope == null || !envelope.isObject()) {
                return null;
            }

            if (!VERSION.equals(envelope.path(FIELD_VERSION).asText())) {
                return null;
            }

            JavaType targetType =
                    supportedTypes.get(envelope.path(FIELD_TYPE).asText());

            if (targetType == null) {
                return null;
            }

            JsonNode payload = envelope.get(FIELD_VALUE);

            if (payload == null || payload.isNull()) {
                return null;
            }

            return objectMapper.convertValue(payload, targetType);
        } catch (Exception exception) {
            // Old or incompatible entries are treated as a cache miss. The
            // caller repopulates the same key instead of failing the request.
            logger.warn("Discarding unreadable Redis cache entry: {}",
                    exception.getMessage());
            return null;
        }
    }

    private String resolveType(Object value) {

        if (value instanceof String) {
            return TYPE_STRING;
        }

        if (value instanceof NoteResponse) {
            return TYPE_NOTE;
        }

        if (value instanceof List<?> list &&
                list.stream().allMatch(item ->
                        item == null || item instanceof NoteResponse)) {
            return TYPE_NOTE_LIST;
        }

        throw new SerializationException(
                "Unsupported Redis cache value type: "
                        + value.getClass().getName());
    }
}
