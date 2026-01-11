package io.github.manjago.mz4d.domain.outbox;

import io.github.manjago.mz4d.domain.message.ReceivedMessage;
import io.github.manjago.mz4d.persistence.MvStoreManager;
import io.github.manjago.mz4d.persistence.repository.OutboxRepository;
import io.github.manjago.mz4d.persistence.serialization.JsonDataSerializer;
import org.jetbrains.annotations.NotNull;


public class OutBoxWorkerProofOfConcept {

    private final MvStoreManager mvStoreManager;
    private final OutboxRepository outboxRepository;
    private final JsonDataSerializer jsonDataSerializer;

    public OutBoxWorkerProofOfConcept(MvStoreManager mvStoreManager, OutboxRepository outboxRepository,
            JsonDataSerializer jsonDataSerializer) {
        this.mvStoreManager = mvStoreManager;
        this.outboxRepository = outboxRepository;
        this.jsonDataSerializer = jsonDataSerializer;
    }

    // Где-то в фоновом потоке
    public void loopForSend() {
        mvStoreManager.runInTransaction(tx -> {
            outboxRepository.findAll(tx)
                    .forEach(task -> {
                        // Распаковываем
                        Object payload = extractPayload(task);

                        if (payload instanceof ReceivedMessage msg) {
                            // Отправляем в Telegram...
                            System.out.println("Sending: " + msg.text());
                        }
                        // или как-то по другому выбираем

                        // Обновляем мету, если что-то пошло не так и т.п.
                        var newMeta = task.meta().incRetryCount();

                        // Сохраняем обновленный таск
                        outboxRepository.update(tx, task.withMeta(newMeta));
                    });
        });
    }

    // и где-то в фоновом потоке  public void loopForDeleteExpired()

    private <T> T extractPayload(@NotNull OutboxTask task) {
        final Class<?> clazz = task.type().getTargetClass();
        return (T) jsonDataSerializer.deserialize(task.payloadJson(), clazz);
    }

}
