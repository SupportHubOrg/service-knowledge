package com.supporthub.knowledge.messaging.outbox.core.publisher;

import com.supporthub.knowledge.messaging.outbox.core.model.OutboxEventRecord;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxEventSender;
import com.supporthub.knowledge.messaging.outbox.core.spi.OutboxSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/** Kafka adapter for the {@link OutboxEventSender} SPI. */
@Component
@RequiredArgsConstructor
public class KafkaOutboxEventSender implements OutboxEventSender {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public CompletableFuture<OutboxSendResult> send(OutboxEventRecord event) {
        return kafkaTemplate.send(event.route().topic(), event.route().messageKey(), event.payload())
                .thenApply(result -> {
                    var metadata = result.getRecordMetadata();
                    return new OutboxSendResult(metadata.topic(), metadata.partition(), metadata.offset());
                });
    }
}
