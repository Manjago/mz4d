package io.github.manjago.mz4d.it;

import com.fasterxml.uuid.Generators;
import io.github.manjago.mz4d.domain.message.IncomingMessage;
import io.github.manjago.mz4d.persistence.MvStoreManager;
import io.github.manjago.mz4d.persistence.repository.IncomingMessageRepository;
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

class IncomingMessageIntegrationTest {

    @Test
    void storeAndLoad(@TempDir Path tempDir) {

        try (MvStoreManager mvStoreManager = new MvStoreManager(tempDir, "test.mv")) {

            //given

            final JsonDataSerializer jsonDataSerializer = new JsonDataSerializer();
            final IncomingMessageRepository incomingMessageRepository =
                    new IncomingMessageRepository(jsonDataSerializer);

            final UUID traceId = Generators.timeBasedEpochGenerator().generate();

            final long userId = 123L;
            final String text = "Hi!";
            final IncomingMessage storedIncomingMessage = new IncomingMessage(userId, text);

            // сохраняем
            final Transaction tx1 = mvStoreManager.beginTransaction();
            incomingMessageRepository.save(tx1, traceId, storedIncomingMessage);
            tx1.commit();

            // находим
            final Transaction tx2 = mvStoreManager.beginTransaction();
            final Optional<IncomingMessage> loadedIncomingMessageOpt = incomingMessageRepository.findByTraceId(tx2, traceId);
            tx2.commit();

            assertTrue(loadedIncomingMessageOpt.isPresent());
            final IncomingMessage loadedIncomingMessage = loadedIncomingMessageOpt.get();
            assertEquals(userId, loadedIncomingMessage.userId());
            assertEquals(text, loadedIncomingMessage.text());

            // удаляем
            final Transaction tx3 = mvStoreManager.beginTransaction();
            incomingMessageRepository.delete(tx3, traceId);
            tx3.commit();

            //и потом не находим
            final Transaction tx4 = mvStoreManager.beginTransaction();
            final Optional<IncomingMessage> loadedIncomingMessageOptNotFound = incomingMessageRepository.findByTraceId(tx4, traceId);
            tx4.commit();

            assertFalse(loadedIncomingMessageOptNotFound.isPresent());

            //и мы можем удалять несуществующее без исключений
            final Transaction tx5 = mvStoreManager.beginTransaction();
            incomingMessageRepository.delete(tx5, traceId);
            tx5.commit();

        }
    }
}
