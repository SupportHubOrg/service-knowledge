package com.supporthub.knowledge.messaging.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the integration event payload component that supplies the Kafka message key (partition key).
 * All events for one aggregate share a key so they keep their order on a single partition. Each
 * {@link IntegrationEvent} payload must declare exactly one {@code @MessageKey} component.
 */
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageKey {
}
