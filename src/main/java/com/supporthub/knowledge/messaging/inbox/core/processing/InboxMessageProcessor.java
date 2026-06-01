package com.supporthub.knowledge.messaging.inbox.core.processing;

import com.supporthub.knowledge.messaging.error.NonRetryableKafkaProcessingException;
import com.supporthub.knowledge.messaging.error.RetryableKafkaProcessingException;
import com.supporthub.knowledge.messaging.inbox.core.context.InboxEventContext;
import com.supporthub.knowledge.messaging.inbox.core.error.InboxHandlerNotFoundException;
import com.supporthub.knowledge.messaging.inbox.core.error.InboxMessageFormatException;
import com.supporthub.knowledge.messaging.inbox.core.handler.InboxHandlerKey;
import com.supporthub.knowledge.messaging.inbox.core.handler.InboxHandlerRegistry;
import com.supporthub.knowledge.messaging.inbox.core.model.InboxEventEnvelope;
import com.supporthub.knowledge.messaging.inbox.core.persistence.InboxEventStore;
import com.supporthub.knowledge.messaging.inbox.core.serialization.InboxEventEnvelopeReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxMessageProcessor {

    private final InboxEventEnvelopeReader envelopeReader;
    private final InboxHandlerRegistry handlerRegistry;
    private final InboxEventStore eventStore;

    @Transactional
    public void process(String consumer, ConsumerRecord<String, String> consumerRecord) {
        var eventType = "Unknown";
        try {
            var envelope = envelopeReader.read(consumerRecord.value());
            var context = context(consumer, consumerRecord, envelope);
            eventType = context.eventType();

            if (!eventStore.insertIfNew(context)) {
                log.info("Duplicate inbox event skipped. consumer={}, eventType={}, eventId={}, topic={}, partition={}, offset={}",
                        consumer,
                        context.eventType(),
                        context.eventId(),
                        context.topic(),
                        context.partition(),
                        context.offset());
                return;
            }

            var handler = handlerRegistry.get(new InboxHandlerKey(
                    consumer,
                    context.eventType(),
                    context.eventVersion()
            ));
            var payload = envelopeReader.payloadAs(envelope.payload(), handler.payloadType());
            handler.invoke(payload, context);
            log.info("Processed inbox event. consumer={}, eventType={}, eventId={}, topic={}, partition={}, offset={}",
                    consumer,
                    context.eventType(),
                    context.eventId(),
                    context.topic(),
                    context.partition(),
                    context.offset());
        } catch (InboxMessageFormatException | InboxHandlerNotFoundException exception) {
            throw new NonRetryableKafkaProcessingException("Non-retryable inbox event " + eventType, exception);
        } catch (NonRetryableKafkaProcessingException | RetryableKafkaProcessingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new RetryableKafkaProcessingException("Failed to process inbox event " + eventType, exception);
        }
    }

    private InboxEventContext context(String consumer,
                                      ConsumerRecord<String, String> consumerRecord,
                                      InboxEventEnvelope envelope) {
        var metadata = envelope.metadata();
        return new InboxEventContext(
                consumer,
                metadata.eventId(),
                metadata.eventType(),
                metadata.version(),
                metadata.timestamp(),
                metadata.source(),
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                consumerRecord.key()
        );
    }
}
