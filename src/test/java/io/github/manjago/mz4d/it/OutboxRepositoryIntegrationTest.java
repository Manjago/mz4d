package io.github.manjago.mz4d.it;

import com.fasterxml.uuid.Generators;
import io.github.manjago.mz4d.config.Mz4dConfig;
import io.github.manjago.mz4d.domain.message.OutgoingMessage;
import io.github.manjago.mz4d.domain.message.ReceivedMessage;
import io.github.manjago.mz4d.domain.outbox.OutboxMessageType;
import io.github.manjago.mz4d.domain.outbox.OutboxTask;
import io.github.manjago.mz4d.persistence.MvStoreManager;
import io.github.manjago.mz4d.persistence.repository.OutboxRepository;
import io.github.manjago.mz4d.persistence.serialization.JsonDataSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxRepositoryIntegrationTest {

    private final JsonDataSerializer jsonDataSerializer = new JsonDataSerializer();
    private static final long USER_ID = 123L;
    private static final String TEXT = "Hi!";

    @Test
    void storeAndLoad(@TempDir Path tempDir){

        final OutboxRepository repository =
                new OutboxRepository(jsonDataSerializer, Mz4dConfig.defaults());

        try (MvStoreManager mvStoreManager = new MvStoreManager(tempDir, "test.mv")) {

            //given

            final UUID traceId = Generators.timeBasedEpochGenerator().generate();

            final ReceivedMessage storedReceivedMessage = new ReceivedMessage(USER_ID, TEXT);

            // сохраняем
            mvStoreManager.runInTransaction(tx -> repository.schedule(tx, storedReceivedMessage, traceId));

            // находим
            final Optional<OutboxTask> storedOutboxTaskOpt = mvStoreManager.runInTransactionWithResult(tx -> repository.findById(tx, traceId));

            final OutboxTask outboxTask = check(traceId, 0, storedOutboxTaskOpt);

            // обновим мету
            mvStoreManager.runInTransaction(tx -> repository.update(tx, outboxTask.withMeta(outboxTask.meta().incRetryCount())));

            // находим ее снова
            final Optional<OutboxTask> storedOutboxTaskOpt2 = mvStoreManager.runInTransactionWithResult(tx -> repository.findById(tx, traceId));
            check(traceId, 1, storedOutboxTaskOpt2);

            // найдем все
            final List<OutboxTask> list = mvStoreManager.runInTransactionWithResult(repository::findPending).toList();
            assertNotNull(list);
            assertEquals(1, list.size());
            check(traceId,  1, Optional.of(list.getFirst()));

            // удаляем
            mvStoreManager.runInTransaction(tx -> repository.delete(tx, traceId));

            // и уже ничего не находим
            final List<OutboxTask> list2 = mvStoreManager.runInTransactionWithResult(repository::findAll).toList();
            assertNotNull(list2);
            assertTrue(list2.isEmpty());
        }
    }

    private <T> T extractPayload(@NotNull OutboxTask task) {
        final Class<?> clazz = task.type().getTargetClass();
        return (T) jsonDataSerializer.deserialize(task.payloadJson(), clazz);
    }

    private @NotNull OutboxTask check(UUID traceId, int retryCount,  Optional<OutboxTask> storedOutboxTaskOpt) {
        assertNotNull(storedOutboxTaskOpt);
        assertTrue(storedOutboxTaskOpt.isPresent());
        final OutboxTask outboxTask = storedOutboxTaskOpt.get();
        assertEquals(traceId, outboxTask.traceId());
        assertEquals(retryCount, outboxTask.meta().retryCount());
        assertNotNull(outboxTask.meta().createdAt());
        assertEquals(OutboxMessageType.EXTERNAL_IN, outboxTask.type());
        assertNotNull(outboxTask.payloadJson());

        final ReceivedMessage loadedReceivedMessage = extractPayload(outboxTask);
        assertEquals(USER_ID, loadedReceivedMessage.userId());
        assertEquals(TEXT, loadedReceivedMessage.text());
        return outboxTask;
    }
}
