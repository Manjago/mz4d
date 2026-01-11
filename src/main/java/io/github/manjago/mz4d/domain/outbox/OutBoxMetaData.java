package io.github.manjago.mz4d.domain.outbox;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Метаданные для Transaction Outbox
 * @param retryCount сколько раз пытались отправить
 * @param createdAt когда создана
 */
public record OutBoxMetaData(
        int retryCount,        // сколько раз пытались отправить
        Instant createdAt
) {
    public @NotNull OutBoxMetaData incRetryCount() {
        return new OutBoxMetaData(retryCount + 1, createdAt);
    }

}