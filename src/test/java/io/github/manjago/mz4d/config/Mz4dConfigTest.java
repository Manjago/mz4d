package io.github.manjago.mz4d.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class Mz4dConfigTest {

    @Test
    void defaults() {
        final Mz4dConfig config = Mz4dConfig.defaults();
        System.out.println(config);
        assertEquals("data/mvstore", config.mvstorePath().toString());
        assertEquals("game.mv.db", config.mvstoreFileName());
        assertEquals("data/artemis", config.artemisPath().toString());
        assertEquals(5, config.outBoxMaxRetries());
        assertEquals(3, config.outBoxTtlDays());
    }

    @Test
    void fromFileShouldMergeAndOverrideDefaults(@TempDir Path tempDir) throws IOException {
        final String customConfigContent = """
                mz4d {
                  mvstore {
                    file = "custom-game.db"
                  }
                  outbox {
                    max-retries = 99
                  }
                }
                """;

        final Path customConfigFile = tempDir.resolve("custom.conf");
        Files.writeString(customConfigFile, customConfigContent);

        final Mz4dConfig config = Mz4dConfig.fromFile(customConfigFile);

        assertEquals("custom-game.db", config.mvstoreFileName(),
                "Значение из файла должно перекрыть дефолтное");
        assertEquals(99, config.outBoxMaxRetries(),
                "Значение из файла должно перекрыть дефолтное");

        assertEquals("data/mvstore", config.mvstorePath().toString(),
                "Если в файле нет значения, должно браться дефолтное");
        assertEquals("data/artemis", config.artemisPath().toString(),
                "Если в файле нет значения, должно браться дефолтное");
        assertEquals(3, config.outBoxTtlDays());
    }
}