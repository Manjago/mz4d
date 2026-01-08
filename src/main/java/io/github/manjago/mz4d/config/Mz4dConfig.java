package io.github.manjago.mz4d.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record Mz4dConfig(Path mvstorePath, String mvstoreFileName, Path artemisPath) {

    /**
     * Load default configuration.
     */
    public static @NotNull Mz4dConfig defaults() {
        return fromConfig(ConfigFactory.load());
    }

    private static @NotNull Mz4dConfig fromConfig(@NotNull Config config) {
        final Config c = config.getConfig("mz4d");

        return new Mz4dConfig(Path.of(c.getString("mvstore.path")), c.getString("mvstore.file"), Path.of(c.getString(
                "artemis.path")));
    }

}
