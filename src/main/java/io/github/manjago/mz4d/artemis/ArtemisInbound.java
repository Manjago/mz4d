package io.github.manjago.mz4d.artemis;

import io.github.manjago.mz4d.domain.outbox.OutboxConsumer;
import io.github.manjago.mz4d.domain.outbox.OutboxTask;
import org.jetbrains.annotations.NotNull;

public class ArtemisInbound implements OutboxConsumer {

    @Override
    public boolean send(@NotNull OutboxTask outboxTask) {
        //todo implement this
        return false;
    }
}
