package io.github.manjago.mz4d.domain.outbox;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record OutboxTask(
        UUID traceId,                // Уникальный ID задачи (он же ключ в Map)
        OutBoxMetaData meta,    // Метаданные (retry, dates)
        OutboxMessageType type,  // Тип данных
        String payloadJson      // Сериализованный бизнес-объект
) {
    @Contract("_ -> new")
    public @NotNull OutboxTask withMeta(OutBoxMetaData newMeta) {
        return new OutboxTask(traceId, newMeta, type, payloadJson);
    }
}