package io.github.manjago.mz4d.persistence;

import org.h2.mvstore.MVStore;

import java.nio.file.Path;

public class MvStoreManager {

    private final Path storePath;
    private final String storeFileName;
    private MVStore store;

    public MvStoreManager(Path storePath, String storeFileName) {
        this.storePath = storePath;
        this.storeFileName = storeFileName;
    }

    public void init() {

        final Path fullPath = storePath.resolve(storeFileName);

        store = new MVStore.Builder().fileName(fullPath.toString()).compress().open();
    }

    public void stop() {
        store.close();
    }
}
