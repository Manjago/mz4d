package io.github.manjago.mz4d.domain.message;

/**
 * Получено от внешнего источника (Telegram/Console)
 * @param userId чат в тг
 * @param text текст
 */
public record ReceivedMessage(long userId, String text) {
}
