package io.github.manjago.mz4d.domain.outbox;

import io.github.manjago.mz4d.config.Mz4dConfig;
import io.github.manjago.mz4d.persistence.MvStoreManager;
import io.github.manjago.mz4d.persistence.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);

    private final MvStoreManager mvStoreManager;
    private final OutboxRepository outboxRepository;
    private final OutboxConsumer outboxConsumer;
    private final Mz4dConfig config;

    public OutboxWorker(MvStoreManager mvStoreManager,
            OutboxRepository outboxRepository,
            OutboxConsumer outboxConsumer, Mz4dConfig config) {
        this.mvStoreManager = mvStoreManager;
        this.outboxRepository = outboxRepository;
        this.outboxConsumer = outboxConsumer;
        this.config = config;
    }

    // предназначена для использования где-то в выделенном потоке
    public void processPending() {
        mvStoreManager.runInTransaction(tx -> outboxRepository.findPending(tx)
                .forEach(task -> {

                    final boolean sent = outboxConsumer.send(task);
                    if (sent) {
                        outboxRepository.delete(tx, task.traceId());  // Успех - удаляем
                    } else if (task.meta().retryCount() < config.outBoxMaxRetries()) {
                        outboxRepository.update(tx, task.withMeta(task.meta().incRetryCount()));  // Retry
                    } else {
                        // Превышен лимит retry
                        log.error("Failed after {} retries: {}", config.outBoxMaxRetries(), task.traceId());
                        outboxRepository.delete(tx, task.traceId());  // Удаляем безнадежную задачу
                    }

                }));

    }

}
