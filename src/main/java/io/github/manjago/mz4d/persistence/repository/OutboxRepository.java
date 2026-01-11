package io.github.manjago.mz4d.persistence.repository;

import io.github.manjago.mz4d.config.Mz4dConfig;
import io.github.manjago.mz4d.domain.outbox.OutBoxMetaData;
import io.github.manjago.mz4d.domain.outbox.OutboxMessageType;
import io.github.manjago.mz4d.domain.outbox.OutboxTask;
import io.github.manjago.mz4d.persistence.serialization.JsonDataSerializer;
import org.h2.mvstore.tx.Transaction;
import org.h2.mvstore.tx.TransactionMap;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class OutboxRepository {

    private static final String MAP_NAME = "transaction_outbox";
    private final JsonDataSerializer serializer;
    private final Mz4dConfig config;

    public OutboxRepository(JsonDataSerializer serializer, Mz4dConfig config) {
        this.serializer = serializer;
        this.config = config;
    }

    // Метод сохранения ЛЮБОГО сообщения
    public <T> void schedule(@NotNull Transaction tx, @NotNull T payload, @NotNull UUID traceId) {

        final String payloadJson = serializer.serialize(payload);

        // Определяем тип через наш реестр
        final OutboxMessageType type = OutboxMessageType.fromClass(payload.getClass());

        final Instant now = Instant.now();
        final OutBoxMetaData meta = new OutBoxMetaData(0, now);

        // Сохраняем Enum
        final OutboxTask task = new OutboxTask(traceId, meta, type, payloadJson);

        final String taskJson = serializer.serialize(task);
        final TransactionMap<UUID, String> map = tx.openMap(MAP_NAME);
        map.put(traceId, taskJson);
    }

    // Метод для обновления (например, увеличили retryCount)
    public void update(@NotNull Transaction tx, @NotNull OutboxTask task) {
        final TransactionMap<UUID, String> map = tx.openMap(MAP_NAME);
        final String json = serializer.serialize(task);
        map.put(task.traceId(), json);
    }

    // Получение задачи по ID
    public Optional<OutboxTask> findById(@NotNull Transaction tx, @NotNull UUID id) {
        final TransactionMap<UUID, String> map = tx.openMap(MAP_NAME);
        final String json = map.get(id);
        if (json == null)
            return Optional.empty();

        return Optional.of(serializer.deserialize(json, OutboxTask.class));
    }

    // Найти все (для воркера, который будет разгребать)
    // В MVStore итераторы ленивые, это хорошо
    public Stream<OutboxTask> findAll(@NotNull Transaction tx) {
        TransactionMap<UUID, String> map = tx.openMap(MAP_NAME);

        Iterator<UUID> keys = map.keyIterator(null);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(keys, Spliterator.ORDERED), false).map(uuid -> {
            String json = map.get(uuid);
            return serializer.deserialize(json, OutboxTask.class);
        });
    }

    public void delete(@NotNull Transaction tx, UUID traceId) {
        final TransactionMap<UUID, String> map = tx.openMap(MAP_NAME);
        map.remove(traceId);
    }

    public Stream<OutboxTask> findPending(Transaction tx) {
        return findAll(tx);
    }
}