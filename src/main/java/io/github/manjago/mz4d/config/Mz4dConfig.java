package io.github.manjago.mz4d.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record Mz4dConfig(
        Path mvstorePath,
        String mvstoreFileName,
        Path artemisPath,
        int outBoxMaxRetries,
        int deduplicationTtlDays
) {

    /**
     * Load default configuration.
     */
    public static @NotNull Mz4dConfig defaults() {
        return fromConfig(ConfigFactory.load());
    }

    public static @NotNull Mz4dConfig fromFile(@NotNull Path configFile) {
        final Config fileConfig = ConfigFactory.parseFile(configFile.toFile());
        final Config merged = fileConfig.withFallback(ConfigFactory.load());
        return fromConfig(merged);
    }

    private static @NotNull Mz4dConfig fromConfig(@NotNull Config config) {
        final Config c = config.getConfig("mz4d");

        // @formatter:off
        return new Mz4dConfig(
                Path.of(c.getString("mvstore.path")),
                c.getString("mvstore.file"),
                Path.of(c.getString("artemis.path")),
                c.getInt("outbox.max-retries"),
                c.getInt("deduplication.ttl-days")
        );
        // @formatter:on

    }

}
