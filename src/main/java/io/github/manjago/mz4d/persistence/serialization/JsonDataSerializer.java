package io.github.manjago.mz4d.persistence.serialization;

import io.github.manjago.mz4d.exceptions.Mz4dPanicException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Сериализует класс в json и обратно.
 * Обертка над jackson - перевыбрасываем свои исключения
 */
public class JsonDataSerializer {
    private final ObjectMapper mapper = new ObjectMapper();

    public <T> String serialize(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new Mz4dPanicException("Serialization failed for class: " + object.getClass().getName(), e);
        }
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JacksonException e) {
            throw new Mz4dPanicException("Deserialization failed for class: " + clazz.getName(), e);
        }
    }
}
