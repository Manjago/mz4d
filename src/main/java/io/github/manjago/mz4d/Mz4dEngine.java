package io.github.manjago.mz4d;

import io.github.manjago.mz4d.config.Mz4dConfig;
import io.github.manjago.mz4d.persistence.MvStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class Mz4dEngine {

    private static final Logger log = LoggerFactory.getLogger(Mz4dEngine.class);
    // Control
    private final AtomicBoolean running = new AtomicBoolean(false);

    private MvStoreManager mvStoreManager;

    @SuppressWarnings("java:S6881")
    public boolean run() {

        if (running.getAndSet(true)) {
            log.warn("Simulator already running");
            return false;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (running.get()) {
                log.info("\n⏸️  Stopping gracefully...");
                stop();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.warn("Interrupted!", e);
                    Thread.currentThread().interrupt();
                }
            }
        }));

        final Mz4dConfig mz4dConfig = Mz4dConfig.defaults();
        mvStoreManager = new MvStoreManager(mz4dConfig.mvstorePath(), mz4dConfig.mvstoreFileName());

        return true;
    }

    public void stop() {
        mvStoreManager.stop();
    }
}
