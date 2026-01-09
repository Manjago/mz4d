package io.github.manjago.mz4d.it;

import com.fasterxml.uuid.Generators;
import io.github.manjago.mz4d.domain.message.GameCommand;
import io.github.manjago.mz4d.persistence.MvStoreManager;
import io.github.manjago.mz4d.persistence.repository.GameCommandRepository;
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

class GameCommandIntegrationTest {

    @Test
    void storeAndLoad(@TempDir Path tempDir) {

        try (MvStoreManager mvStoreManager = new MvStoreManager(tempDir, "test.mv")) {

            //given

            final JsonDataSerializer jsonDataSerializer = new JsonDataSerializer();
            final GameCommandRepository gameCommandRepository = new GameCommandRepository(jsonDataSerializer);

            final UUID traceId = Generators.timeBasedEpochGenerator().generate();

            final long userId = 123L;
            final String text = "Hi!";
            final GameCommand storedGameCommand = new GameCommand(userId, text);

            // сохраняем
            mvStoreManager.runInTransaction(tx -> gameCommandRepository.save(tx, traceId, storedGameCommand));

            // находим
            final Optional<GameCommand> loadedIncomingMessageOpt =
                    mvStoreManager.runInTransactionWithResult(tx -> gameCommandRepository.findByTraceId(tx, traceId));

            assertTrue(loadedIncomingMessageOpt.isPresent());
            final GameCommand loadedGameCommand = loadedIncomingMessageOpt.get();
            assertEquals(userId, loadedGameCommand.userId());
            assertEquals(text, loadedGameCommand.text());

            // удаляем
            mvStoreManager.runInTransaction(tx -> gameCommandRepository.delete(tx, traceId));

            //и потом не находим
            final Optional<GameCommand> loadedIncomingMessageOptNotFound =
                    mvStoreManager.runInTransactionWithResult(tx -> gameCommandRepository.findByTraceId(tx, traceId));

            assertFalse(loadedIncomingMessageOptNotFound.isPresent());

            //и мы можем удалять несуществующее без исключений
            final Transaction tx5 = mvStoreManager.beginTransaction();
            gameCommandRepository.delete(tx5, traceId);
            tx5.commit();

        }
    }
}
