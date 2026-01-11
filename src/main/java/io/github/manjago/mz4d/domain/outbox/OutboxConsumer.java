package io.github.manjago.mz4d.domain.outbox;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface OutboxConsumer {
    boolean send(@NotNull OutboxTask outboxTask);
}
