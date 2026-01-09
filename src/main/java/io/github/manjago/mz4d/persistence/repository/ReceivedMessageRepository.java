package io.github.manjago.mz4d.persistence.repository;

import io.github.manjago.mz4d.domain.message.ReceivedMessage;
import io.github.manjago.mz4d.persistence.serialization.JsonDataSerializer;
import org.h2.mvstore.tx.Transaction;
import org.h2.mvstore.tx.TransactionMap;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ReceivedMessageRepository {

    private static final String MAP_NAME = "incoming_msg";

    private final JsonDataSerializer serializer;

    public ReceivedMessageRepository(JsonDataSerializer serializer) {
        this.serializer = serializer;
    }

    public void save(@NotNull Transaction tx, @NotNull UUID traceId, @NotNull ReceivedMessage message) {
        final TransactionMap<UUID, String> map = tx.openMap(MAP_NAME);
        final String json = serializer.serialize(message);
        map.put(traceId, json);
    }

    public Optional<ReceivedMessage> findByTraceId(@NotNull Transaction tx, @NotNull UUID traceId) {
        final TransactionMap<UUID, String> map = tx.openMap(MAP_NAME);
        final String json = map.get(traceId);
        if (json == null) {
            return Optional.empty();
        }
        final ReceivedMessage receivedMessage = serializer.deserialize(json, ReceivedMessage.class);
        return Optional.of(receivedMessage);
    }

    public void delete(@NotNull Transaction tx, UUID traceId) {
        final TransactionMap<UUID, String> map = tx.openMap(MAP_NAME);
        map.remove(traceId);
    }
}
