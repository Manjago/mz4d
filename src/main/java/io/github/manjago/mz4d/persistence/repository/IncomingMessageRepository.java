package io.github.manjago.mz4d.persistence.repository;

import io.github.manjago.mz4d.domain.message.IncomingMessage;

import java.util.Optional;
import java.util.UUID;

public interface IncomingMessageRepository {
    void save(UUID traceId, IncomingMessage message);
    Optional<IncomingMessage> findByTraceId(UUID traceId);
    void delete(UUID traceId);
}
