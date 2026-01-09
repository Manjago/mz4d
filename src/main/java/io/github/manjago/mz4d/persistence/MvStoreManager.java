package io.github.manjago.mz4d.persistence;

import io.github.manjago.mz4d.exceptions.Mz4dPanicException;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.tx.Transaction;
import org.h2.mvstore.tx.TransactionStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MvStoreManager {

    private final Path storePath;
    private final String storeFileName;
    // Держим ссылку на raw store, чтобы закрыть его в конце
    private MVStore store;
    // Держим ссылку на txStore, чтобы создавать транзакции
    private TransactionStore txStore;

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

        // 1. Открываем сырой store
        store = new MVStore.Builder().fileName(fullPath.toString()).compress()
                // autoCommitBufferSize сбрасывает данные на диск в фоне,
                // чтобы файл не рос бесконечно в памяти
                .autoCommitBufferSize(1024) // не вижу смысла давать пользователю кастомизировать
                .open();

        // 2. Инициализируем слой транзакций поверх него
        txStore = new TransactionStore(store);
        txStore.init();

        return this;
    }

    public void stop() {
        // Достаточно закрыть основной store, транзакционный слой закроется с ним
        if (store != null) {
            store.close();
            store = null;
            txStore = null;
        }
    }

    // Главный метод для бизнес-логики
    public Transaction beginTransaction() {
        if (txStore == null) {
            throw new Mz4dPanicException("Store not initialized");
        }
        return txStore.begin();
    }
}
