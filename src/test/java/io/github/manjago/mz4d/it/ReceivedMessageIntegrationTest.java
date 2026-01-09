package io.github.manjago.mz4d.it;

import com.fasterxml.uuid.Generators;
import io.github.manjago.mz4d.domain.message.ReceivedMessage;
import io.github.manjago.mz4d.persistence.MvStoreManager;
import io.github.manjago.mz4d.persistence.repository.ReceivedMessageRepository;
import io.github.manjago.mz4d.persistence.serialization.JsonDataSerializer;
import org.h2.mvstore.tx.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceivedMessageIntegrationTest {

    @Test
    void storeAndLoad(@TempDir Path tempDir) {

        try (MvStoreManager mvStoreManager = new MvStoreManager(tempDir, "test.mv")) {

            //given

            final JsonDataSerializer jsonDataSerializer = new JsonDataSerializer();
            final ReceivedMessageRepository receivedMessageRepository =
                    new ReceivedMessageRepository(jsonDataSerializer);

            final UUID traceId = Generators.timeBasedEpochGenerator().generate();

            final long userId = 123L;
            final String text = "Hi!";
            final ReceivedMessage storedReceivedMessage = new ReceivedMessage(userId, text);

            // сохраняем
            mvStoreManager.runInTransaction(tx -> receivedMessageRepository.save(tx, traceId, storedReceivedMessage));

            // находим
            final Optional<ReceivedMessage> loadedIncomingMessageOpt =
                    mvStoreManager.runInTransactionWithResult(tx -> receivedMessageRepository.findByTraceId(tx,
                            traceId));

            assertTrue(loadedIncomingMessageOpt.isPresent());
            final ReceivedMessage loadedReceivedMessage = loadedIncomingMessageOpt.get();
            assertEquals(userId, loadedReceivedMessage.userId());
            assertEquals(text, loadedReceivedMessage.text());

            // удаляем
            mvStoreManager.runInTransaction(tx -> receivedMessageRepository.delete(tx, traceId));

            //и потом не находим
            final Optional<ReceivedMessage> loadedIncomingMessageOptNotFound =
                    mvStoreManager.runInTransactionWithResult(tx -> receivedMessageRepository.findByTraceId(tx,
                            traceId));

            assertFalse(loadedIncomingMessageOptNotFound.isPresent());

            //и мы можем удалять несуществующее без исключений
            final Transaction tx5 = mvStoreManager.beginTransaction();
            receivedMessageRepository.delete(tx5, traceId);
            tx5.commit();

        }
    }
}
