package io.github.manjago.mz4d.persistence.repository;

import io.github.manjago.mz4d.persistence.MvStoreManager;
import org.h2.mvstore.tx.TransactionMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class DeduplicationRepository {

    private static final Logger log = LoggerFactory.getLogger(DeduplicationRepository.class);
    private static final String MAP_DATA_PREFIX = "dedup_data";
    private static final String MAP_INDEX_PREFIX = "dedup_index";
    private static final int CHUNK_SIZE = 2000; // Оптимальный размер для удаления
    private final MvStoreManager mvStoreManager;
    public DeduplicationRepository(MvStoreManager mvStoreManager) {
        this.mvStoreManager = mvStoreManager;
    }

    public void add(@NotNull String storeId, @NotNull UUID id, @NotNull Instant expiredAt) {

        mvStoreManager.runInTransaction(tx -> {
            final TransactionMap<UUID, Instant> dataMap = tx.openMap(mapData(storeId));
            TransactionMap<ExpirationKey, Boolean> indexMap = tx.openMap(mapIndex(storeId));

            // 1. Проверяем, есть ли уже такой UUID (если нужно обновить TTL)
            final Instant oldTime = dataMap.get(id);
            if (oldTime != null) {
                // Если запись была, надо удалить старый индекс, иначе он "повиснет"
                indexMap.remove(new ExpirationKey(oldTime, id));
            }

            // 2. Пишем в основную мапу
            dataMap.put(id, expiredAt);

            // 3. Пишем в индексную мапу (сортированный ключ)
            indexMap.put(new ExpirationKey(expiredAt, id), true);

        });

    }

    public void runCleanupJob(@NotNull String storeId) {
        boolean hasMore = true;

        while (hasMore) {
            // Запускаем очистку одного чанка
            final int deletedCount = cleanupChunk(storeId);

            // Если удалили меньше, чем просили - значит, старых записей больше нет
            if (deletedCount < CHUNK_SIZE) {
                hasMore = false;
            } else {
                // Маленькая пауза, чтобы не нагружать диск на 100%,
                // если удалять приходится миллионы записей подряд.
                // Даем MVStore время на фоновую компактизацию файла.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.warn("Cleanup job interrupted!", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // Метод возвращает количество реально удаленных записей
    private int cleanupChunk(@NotNull String storeId) {

        return mvStoreManager.runInTransactionWithResult(tx -> {
            final TransactionMap<UUID, Instant> dataMap = tx.openMap(mapData(storeId));
            final TransactionMap<ExpirationKey, Boolean> indexMap = tx.openMap(mapIndex(storeId));

            final long now = System.currentTimeMillis();
            final Iterator<ExpirationKey> iterator = indexMap.keyIterator(null);

            int count = 0;
            while (iterator.hasNext() && count < CHUNK_SIZE) {
                ExpirationKey key = iterator.next();
                if (key.timestamp > now) {
                    break; // Дальше только записи из будущего
                }

                dataMap.remove(key.uuid);
                indexMap.remove(key);
                count++;
            }

            return count;

        }, processed -> processed > 0);

    }

    @Contract(pure = true)
    private @NotNull String mapData(@NotNull String storeId) {
        return MAP_DATA_PREFIX + storeId;
    }

    @Contract(pure = true)
    private @NotNull String mapIndex(@NotNull String storeId) {
        return MAP_INDEX_PREFIX + storeId;
    }

    // Класс для составного ключа
    // Важно: должен быть Comparable для правильной сортировки в B-Tree
    public static class ExpirationKey implements Serializable, Comparable<ExpirationKey> {
        public final long timestamp;
        public final UUID uuid;

        public ExpirationKey(long timestamp, UUID uuid) {
            this.timestamp = timestamp;
            this.uuid = uuid;
        }

        public ExpirationKey(@NotNull Instant instant, UUID uuid) {
            this(instant.toEpochMilli(), uuid);
        }

        @Override
        public int compareTo(ExpirationKey o) {
            // Сначала сортируем по времени
            int timeCmp = Long.compare(this.timestamp, o.timestamp);
            if (timeCmp != 0)
                return timeCmp;
            // Если время совпало, сортируем по UUID (чтобы обеспечить уникальность ключа)
            return this.uuid.compareTo(o.uuid);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            ExpirationKey that = (ExpirationKey) o;
            return this.compareTo(that) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, uuid);
        }
    }

}
