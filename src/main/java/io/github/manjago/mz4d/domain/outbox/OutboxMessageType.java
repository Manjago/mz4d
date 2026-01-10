package io.github.manjago.mz4d.domain.outbox;

import io.github.manjago.mz4d.domain.message.GameCommand;
import io.github.manjago.mz4d.domain.message.GameResponse;
import io.github.manjago.mz4d.domain.message.OutgoingMessage;
import io.github.manjago.mz4d.domain.message.ReceivedMessage;
import org.jetbrains.annotations.NotNull;

public enum OutboxMessageType {
    // Связываем имя типа с конкретным классом
    EXTERNAL_IN(ReceivedMessage.class),
    GAME_IN(GameCommand.class),
    GAME_OUT(GameResponse.class),
    EXTERNAL_OUT(OutgoingMessage.class)
    ;

    private final Class<?> targetClass;

    OutboxMessageType(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    // Метод для обратного поиска: Class -> Enum
    public static @NotNull OutboxMessageType fromClass(Class<?> clazz) {
        for (OutboxMessageType type : values()) {
            if (type.targetClass.equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No OutboxMessageType registered for class " + clazz.getName());
    }
}