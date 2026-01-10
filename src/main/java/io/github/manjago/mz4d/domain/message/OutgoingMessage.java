package io.github.manjago.mz4d.domain.message;

/**
 * Готово к отправке наружу
 * @param userId чат в тг
 * @param text текст
 */
public record OutgoingMessage(long userId, String text) {
}
