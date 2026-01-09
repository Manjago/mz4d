package io.github.manjago.mz4d.domain.message;

/**
 * Команда для игрового движка
 * @param userId чат в тг (идентификатор пользователя для движка)
 * @param text текст (пока что)
 */
public record GameCommand(long userId, String text) {
}
