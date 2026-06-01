package com.supporthub.knowledge.messaging.inbox.core.persistence;

import com.supporthub.knowledge.messaging.inbox.core.context.InboxEventContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InboxEventStore {

    private static final String INSERT_SQL = """
            insert into inbox_events (
                id,
                consumer_name,
                event_id,
                event_type,
                event_version,
                source,
                topic,
                kafka_partition,
                kafka_offset,
                message_key,
                processed_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            on conflict (consumer_name, event_id) do nothing
            """;

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public boolean insertIfNew(InboxEventContext context) {
        var inserted = jdbcTemplate.update(
                INSERT_SQL,
                UUID.randomUUID(),
                context.consumer(),
                context.eventId(),
                context.eventType(),
                context.eventVersion(),
                context.source(),
                context.topic(),
                context.partition(),
                context.offset(),
                context.messageKey(),
                Timestamp.from(clock.instant())
        );
        return inserted == 1;
    }
}
