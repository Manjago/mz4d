package io.github.manjago.mz4d.persistence.repository;

import io.github.manjago.mz4d.domain.message.IncomingMessage;
import org.h2.mvstore.tx.Transaction;
import org.h2.mvstore.tx.TransactionMap;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class IncomingMessageRepository {

    private static final String MAP_NAME = "incoming_msg";

    public void save(@NotNull Transaction tx, @NotNull UUID traceId, @NotNull IncomingMessage message) {
        final TransactionMap<UUID, IncomingMessage> map = tx.openMap(MAP_NAME);
        map.put(traceId, message);
    }

    public Optional<IncomingMessage> findByTraceId(UUID traceId) {
        return null;
    }

    public void delete(UUID traceId) {

    }
}
