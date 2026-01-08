package io.github.manjago.mz4d.persistence.mvstore;

import io.github.manjago.mz4d.exceptions.Mz4dPanicException;
import org.h2.mvstore.MVStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MvStoreManager {

    private final Path storePath;
    private final String storeFileName;
    private MVStore store;

    public MvStoreManager(Path storePath, String storeFileName) {
        this.storePath = storePath;
        this.storeFileName = storeFileName;
    }

    public MvStoreManager init() {
        final Path fullPath = storePath.resolve(storeFileName);
        try {
            Files.createDirectories(storePath);
        } catch (IOException e) {
            throw new Mz4dPanicException("Fail create '" + storePath + "' for MVStore", e);
        }
        store = new MVStore.Builder().fileName(fullPath.toString()).compress().autoCommitBufferSize(1024).open();
        return this;
    }

    public void stop() {
        if (store != null) {
            store.close();
            store = null;
        }
    }
}
