package com.supporthub.knowledge.messaging.consumer;

import com.supporthub.knowledge.draft.application.KnowledgeDraftService;
import com.supporthub.knowledge.messaging.error.NonRetryableKafkaProcessingException;
import com.supporthub.knowledge.messaging.error.RetryableKafkaProcessingException;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Inbound integration point: consumes events from the ticket-management topic.
 *
 * <p>Only {@code KnowledgeArticleProposed} is acted upon — it triggers creation of a draft via
 * {@link KnowledgeDraftService}. Any other event type is ignored by raising a
 * {@link NonRetryableKafkaProcessingException} (it can never be handled here, so it goes to the DLT
 * rather than being retried).
 *
 * <p>Exception mapping drives the retry/DLT policy defined in
 * {@link com.supporthub.knowledge.messaging.config.KafkaConfig}:
 * <ul>
 *   <li>missing event type / unsupported type / missing payload / malformed JSON
 *       → {@link NonRetryableKafkaProcessingException} (straight to DLT);</li>
 *   <li>any other runtime failure (e.g. transient DB error)
 *       → {@link RetryableKafkaProcessingException} (retried, then DLT).</li>
 * </ul>
 * The listener supports both a top-level {@code eventType} and a nested {@code metadata.eventType}
 * for cross-service envelope compatibility.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketManagementEventConsumer {

    private static final String KNOWLEDGE_ARTICLE_PROPOSED = "KnowledgeArticleProposed";

    private final ObjectMapper objectMapper;
    private final KnowledgeDraftService draftService;

    @KafkaListener(
            topics = "${application.kafka.consumer.ticket-management-events}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        var eventType = "Unknown";
        try {
            var root = objectMapper.readTree(consumerRecord.value());
            eventType = textValue(root.path("eventType"));
            if (eventType == null || eventType.isBlank()) {
                eventType = textValue(root.path("metadata").path("eventType"));
            }
            if (eventType == null || eventType.isBlank()) {
                throw new NonRetryableKafkaProcessingException("Kafka eventType is missing");
            }
            if (!KNOWLEDGE_ARTICLE_PROPOSED.equals(eventType)) {
                throw new NonRetryableKafkaProcessingException("Unsupported event type " + eventType);
            }

            var payloadNode = root.path("payload");
            if (payloadNode.isMissingNode() || payloadNode.isNull()) {
                throw new NonRetryableKafkaProcessingException("Kafka event payload is missing");
            }

            var payload = objectMapper.treeToValue(payloadNode, KnowledgeArticleProposedPayload.class);
            draftService.createDraftFromTicketProposal(payload);
            log.info("Processed ticket-management event. eventType={}, key={}, offset={}",
                    eventType, consumerRecord.key(), consumerRecord.offset());
        } catch (tools.jackson.core.JacksonException exception) {
            throw new NonRetryableKafkaProcessingException("Malformed Kafka event " + eventType, exception);
        } catch (NonRetryableKafkaProcessingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new RetryableKafkaProcessingException("Failed to process Kafka event " + eventType, exception);
        }
    }

    private String textValue(tools.jackson.databind.JsonNode node) {
        return node.isString() ? node.asString() : null;
    }
}
