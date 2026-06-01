package com.supporthub.knowledge.messaging.outbox;

import com.supporthub.knowledge.article.api.ArticleResponse;
import com.supporthub.knowledge.draft.api.DraftResponse;
import com.supporthub.knowledge.draft.api.PublishDraftRequest;
import com.supporthub.knowledge.draft.application.KnowledgeDraftService;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleDraftSavedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticleProposedPayload;
import com.supporthub.knowledge.messaging.event.KnowledgeArticlePublishedPayload;
import com.supporthub.knowledge.messaging.outbox.core.persistence.OutboxEventStatus;
import com.supporthub.knowledge.messaging.outbox.core.publisher.OutboxEventPublisher;
import com.supporthub.knowledge.security.CurrentUser;
import com.supporthub.knowledge.security.SupportHubRole;
import com.supporthub.knowledge.support.KnowledgeIntegrationTestSupport;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxKafkaIntegrationTest extends KnowledgeIntegrationTestSupport {

    private final KnowledgeDraftService draftService;
    private final OutboxEventPublisher outboxPublisher;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    OutboxKafkaIntegrationTest(KnowledgeDraftService draftService,
                               OutboxEventPublisher outboxPublisher,
                               JdbcTemplate jdbcTemplate,
                               ObjectMapper objectMapper) {
        this.draftService = draftService;
        this.outboxPublisher = outboxPublisher;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("delete from outbox_events");
        jdbcTemplate.update("delete from knowledge_article_tags");
        jdbcTemplate.update("delete from knowledge_articles");
        jdbcTemplate.update("delete from knowledge_article_drafts");
    }

    @Test
    void shouldPublishOutboxEventsToKafkaInBatchesWithoutSpringTypeHeaders() throws Exception {
        var data = testData();

        try (var consumer = kafkaConsumer()) {
            consumer.subscribe(List.of(KNOWLEDGE_EVENTS_TOPIC));
            seekToTopicEnd(consumer);

            var publishedDraft = createDraftsAndPublishFirst(data);
            assertInitialOutboxRows();

            outboxPublisher.publishPendingEvents();

            var firstBatch = pollRecords(consumer, 2);
            var firstTicketDraftRecord = assertFirstBatch(firstBatch, data, publishedDraft);

            outboxPublisher.publishPendingEvents();

            var secondBatch = pollRecords(consumer, 1);
            assertPublishedEvent(secondBatch.get(0), firstTicketDraftRecord, data, publishedDraft);
            assertAllOutboxRowsSent();
        }
    }

    private TestData testData() {
        return new TestData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }

    private PublishedDraft createDraftsAndPublishFirst(TestData data) {
        var manager = new CurrentUser(
                data.managerId(),
                data.organizationId(),
                Set.of(SupportHubRole.ORGANIZATION_MANAGER)
        );
        var firstDraft = draftService.createDraftFromTicketProposal(new KnowledgeArticleProposedPayload(
                data.firstTicketId(),
                data.organizationId(),
                UUID.randomUUID(),
                "Reset VPN token",
                List.of(data.firstTagId())
        ));
        var publishedArticle = draftService.publishDraft(
                manager,
                firstDraft.id(),
                new PublishDraftRequest("Reset VPN token", "Open the portal and reset the token.")
        );
        draftService.createDraftFromTicketProposal(new KnowledgeArticleProposedPayload(
                data.secondTicketId(),
                data.organizationId(),
                UUID.randomUUID(),
                "Configure mailbox forwarding",
                List.of(data.secondTagId())
        ));
        return new PublishedDraft(firstDraft, publishedArticle);
    }

    private void assertInitialOutboxRows() {
        assertThat(outboxStatusCount(OutboxEventStatus.PENDING)).isEqualTo(3);
        assertThat(outboxStatusCount(OutboxEventStatus.SENT)).isZero();
        assertThat(outboxRows())
                .extracting(row -> row.get("event_type"))
                .containsExactly(
                        KnowledgeArticleDraftSavedPayload.EVENT_TYPE,
                        KnowledgeArticlePublishedPayload.EVENT_TYPE,
                        KnowledgeArticleDraftSavedPayload.EVENT_TYPE
                );
    }

    private ConsumerRecord<String, String> assertFirstBatch(List<ConsumerRecord<String, String>> firstBatch,
                                                           TestData data,
                                                           PublishedDraft publishedDraft) throws Exception {
        assertThat(outboxStatusCount(OutboxEventStatus.SENT)).isEqualTo(2);
        assertThat(outboxStatusCount(OutboxEventStatus.PENDING)).isEqualTo(1);

        var firstTicketDraftRecord = consumerRecordByKey(firstBatch, data.firstTicketId().toString());
        assertDraftSavedEvent(firstTicketDraftRecord, data, publishedDraft.firstDraft());

        var secondTicketDraftRecord = consumerRecordByKey(firstBatch, data.secondTicketId().toString());
        var secondDraftSaved = assertEnvelope(secondTicketDraftRecord, KnowledgeArticleDraftSavedPayload.EVENT_TYPE);
        assertThat(secondDraftSaved.path("payload").path("sourceTicketId").asString())
                .isEqualTo(data.secondTicketId().toString());
        assertThat(secondDraftSaved.path("payload").path("tags")).hasSize(1);
        return firstTicketDraftRecord;
    }

    private void assertDraftSavedEvent(ConsumerRecord<String, String> consumerRecord,
                                       TestData data,
                                       DraftResponse firstDraft) throws Exception {
        var draftSaved = assertEnvelope(consumerRecord, KnowledgeArticleDraftSavedPayload.EVENT_TYPE);
        assertThat(draftSaved.path("payload").path("draftId").asString()).isEqualTo(firstDraft.id().toString());
        assertThat(draftSaved.path("payload").path("sourceTicketId").asString())
                .isEqualTo(data.firstTicketId().toString());
        assertThat(draftSaved.path("payload").path("organizationId").asString())
                .isEqualTo(data.organizationId().toString());
        assertThat(draftSaved.path("payload").path("title").asString()).isEqualTo("Reset VPN token");
        assertThat(draftSaved.path("payload").path("tags")).hasSize(1);
    }

    private void assertPublishedEvent(ConsumerRecord<String, String> consumerRecord,
                                      ConsumerRecord<String, String> firstTicketDraftRecord,
                                      TestData data,
                                      PublishedDraft publishedDraft) throws Exception {
        var articlePublished = assertEnvelope(consumerRecord, KnowledgeArticlePublishedPayload.EVENT_TYPE);
        assertThat(consumerRecord.key()).isEqualTo(data.firstTicketId().toString());
        assertThat(consumerRecord.partition()).isEqualTo(firstTicketDraftRecord.partition());
        assertThat(consumerRecord.offset()).isGreaterThan(firstTicketDraftRecord.offset());
        assertThat(articlePublished.path("payload").path("articleId").asString())
                .isEqualTo(publishedDraft.publishedArticle().id().toString());
        assertThat(articlePublished.path("payload").path("draftId").asString())
                .isEqualTo(publishedDraft.firstDraft().id().toString());
        assertThat(articlePublished.path("payload").path("sourceTicketId").asString())
                .isEqualTo(data.firstTicketId().toString());
        assertThat(articlePublished.path("payload").path("publishedById").asString())
                .isEqualTo(data.managerId().toString());
        assertThat(articlePublished.path("payload").path("articleContent").asString())
                .isEqualTo("Open the portal and reset the token.");
    }

    private void assertAllOutboxRowsSent() {
        assertThat(outboxStatusCount(OutboxEventStatus.SENT)).isEqualTo(3);
        assertThat(outboxStatusCount(OutboxEventStatus.PENDING)).isZero();
        assertThat(outboxRows())
                .allSatisfy(row -> {
                    assertThat(row.get("published_at")).isNotNull();
                    assertThat(row).containsEntry("locked_at", null);
                    assertThat(row).containsEntry("attempts", 0);
                    assertThat(row).containsEntry("last_error", null);
                });
    }

    private KafkaConsumer<String, String> kafkaConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "knowledge-outbox-it-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        ));
    }

    private void seekToTopicEnd(KafkaConsumer<String, String> consumer) {
        var deadline = Instant.now().plusSeconds(20);
        while (consumer.assignment().isEmpty() && Instant.now().isBefore(deadline)) {
            consumer.poll(Duration.ofMillis(250));
        }
        assertThat(consumer.assignment()).isNotEmpty();
        consumer.seekToEnd(consumer.assignment());
        consumer.assignment().forEach(consumer::position);
    }

    private List<ConsumerRecord<String, String>> pollRecords(KafkaConsumer<String, String> consumer, int expectedCount) {
        var records = new ArrayList<ConsumerRecord<String, String>>();
        var deadline = Instant.now().plusSeconds(20);
        while (records.size() < expectedCount && Instant.now().isBefore(deadline)) {
            consumer.poll(Duration.ofMillis(250)).forEach(records::add);
        }
        assertThat(records).hasSize(expectedCount);
        return records;
    }

    private ConsumerRecord<String, String> consumerRecordByKey(List<ConsumerRecord<String, String>> records,
                                                               String key) {
        return records.stream()
                .filter(consumerRecord -> key.equals(consumerRecord.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No record found for key " + key));
    }

    private JsonNode assertEnvelope(ConsumerRecord<String, String> consumerRecord,
                                    String expectedEventType) throws Exception {
        assertNoSpringTypeHeaders(consumerRecord);
        assertThat(consumerRecord.topic()).isEqualTo(KNOWLEDGE_EVENTS_TOPIC);
        assertThat(consumerRecord.partition()).isNotNegative();
        assertThat(consumerRecord.offset()).isNotNegative();

        var root = objectMapper.readTree(consumerRecord.value());
        assertThat(UUID.fromString(root.path("eventId").asString())).isNotNull();
        assertThat(root.path("eventType").asString()).isEqualTo(expectedEventType);
        assertThat(root.path("eventVersion").intValue()).isEqualTo(1);
        assertThat(Instant.parse(root.path("occurredAt").asString())).isNotNull();
        assertThat(root.path("source").asString()).isEqualTo("knowledge-management-service");
        assertThat(root.path("payload").isObject()).isTrue();
        assertThat(root.path("payload").path("eventType").isMissingNode()).isTrue();
        return root;
    }

    private void assertNoSpringTypeHeaders(ConsumerRecord<String, String> consumerRecord) {
        assertThat(consumerRecord.headers().lastHeader("__TypeId__")).isNull();
        assertThat(consumerRecord.headers().lastHeader("__KeyTypeId__")).isNull();
        assertThat(consumerRecord.headers().lastHeader("__ContentTypeId__")).isNull();
    }

    private long outboxStatusCount(OutboxEventStatus status) {
        return jdbcTemplate.queryForObject(
                "select count(*) from outbox_events where status = ?",
                Long.class,
                status.name()
        );
    }

    private List<Map<String, Object>> outboxRows() {
        return jdbcTemplate.queryForList("""
                select event_type, status, attempts, published_at, locked_at, last_error
                from outbox_events
                order by sequence_id
                """);
    }

    private record TestData(
            UUID organizationId,
            UUID managerId,
            UUID firstTicketId,
            UUID secondTicketId,
            UUID firstTagId,
            UUID secondTagId
    ) {
    }

    private record PublishedDraft(
            DraftResponse firstDraft,
            ArticleResponse publishedArticle
    ) {
    }
}
