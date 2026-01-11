package io.github.manjago.mz4d.config;

import org.junit.jupiter.api.Test;

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
        assertEquals(3, config.outBoxTttlDays());
    }
}