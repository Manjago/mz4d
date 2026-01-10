package io.github.manjago.mz4d.domain.message;

/**
 * Ответ от игрового движка
 * @param userId чат в тг (идентификатор пользователя для движка)
 * @param count количество букв
 */
public record GameResponse(long userId, long count) {
}
