package com.supporthub.knowledge.messaging.outbox.core.spi;

import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRecord;

import java.util.concurrent.CompletableFuture;

/** SPI for the transport that delivers outbox events. */
public interface OutboxEventSender {

    CompletableFuture<OutboxSendResult> send(OutboxEventRecord event);
}
