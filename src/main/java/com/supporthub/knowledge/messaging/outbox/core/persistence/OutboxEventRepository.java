package com.supporthub.knowledge.messaging.outbox.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    /**
     * Claims publishable rows in append order, while blocking event N+1 of a
     * {@code (topic, message_key)} until event N is either sent or retryable again.
     */
    @Query(value = """
            select *
            from outbox_events o
            where o.status = 'PENDING'
              and (o.next_attempt_at is null or o.next_attempt_at <= :publishableAt)
              and not exists (
                  select 1
                  from outbox_events e2
                  where e2.topic = o.topic
                    and e2.message_key = o.message_key
                    and e2.sequence_id < o.sequence_id
                    and e2.status in ('PENDING', 'IN_PROGRESS')
              )
            order by o.sequence_id
            limit :limit
            for update skip locked
            """, nativeQuery = true)
    List<OutboxEventEntity> lockPendingForPublishing(@Param("publishableAt") Instant publishableAt,
                                                     @Param("limit") int limit);

    @Modifying
    @Query(value = """
            update outbox_events
            set status = 'PENDING',
                locked_at = null,
                next_attempt_at = :recoveredAt,
                last_error = 'Recovered stuck IN_PROGRESS event',
                updated_at = :recoveredAt
            where status = 'IN_PROGRESS'
              and locked_at < :threshold
            """, nativeQuery = true)
    int recoverStuckEvents(@Param("threshold") Instant threshold, @Param("recoveredAt") Instant recoveredAt);
}
