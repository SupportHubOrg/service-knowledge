package com.supporthub.knowledge.messaging.config;

import com.supporthub.knowledge.messaging.error.NonRetryableKafkaProcessingException;
import com.supporthub.knowledge.messaging.error.RetryableKafkaProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Central Kafka wiring for the service: consumer/producer factories and the listener container
 * with its retry + dead-letter strategy.
 *
 * <p><b>Producer</b> is configured for at-least-once, ordered, idempotent delivery
 * ({@code acks=all}, {@code enable.idempotence=true}, infinite retries, max 5 in-flight requests).
 *
 * <p><b>Error handling</b> is the key part to understand. The listener uses a
 * {@link DefaultErrorHandler} whose behaviour depends on the exception type thrown by the consumer:
 * <ul>
 *   <li>{@link RetryableKafkaProcessingException} — retried in-memory with a
 *       {@link FixedBackOff} (backoff and max attempts come from {@link KafkaDltProperties}).
 *       Used for transient failures (e.g. DB hiccups).</li>
 *   <li>{@link NonRetryableKafkaProcessingException} — never retried; the record is published
 *       straight to the dead-letter topic. Used for poison messages (malformed payload, unknown
 *       event type) that can never succeed.</li>
 * </ul>
 * Once retries are exhausted (or for non-retryable errors) the
 * {@link DeadLetterPublishingRecoverer} routes the record to the DLT, preserving its partition.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {

    private final KafkaDltProperties dltProperties;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:latest}")
    private String autoOffsetReset;

    @Bean
    ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    ProducerFactory<String, String> producerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        properties.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }

    private CommonErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        var errorHandler = new DefaultErrorHandler(
                deadLetterRecoverer(kafkaTemplate),
                new FixedBackOff(dltProperties.backoffMs(), dltProperties.maxAttempts())
        );
        errorHandler.addNotRetryableExceptions(NonRetryableKafkaProcessingException.class);
        errorHandler.addRetryableExceptions(RetryableKafkaProcessingException.class);
        return errorHandler;
    }

    private DeadLetterPublishingRecoverer deadLetterRecoverer(KafkaTemplate<String, String> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (consumerRecord, exception) -> {
            log.warn("Message routed to DLT. topic={}, partition={}, offset={}, key={}",
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    consumerRecord.key(),
                    exception);
            return new TopicPartition(dltProperties.name(), consumerRecord.partition());
        });
    }
}
