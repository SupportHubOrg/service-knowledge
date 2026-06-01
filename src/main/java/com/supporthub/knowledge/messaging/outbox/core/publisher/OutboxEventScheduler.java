package com.supporthub.knowledge.messaging.outbox.core.publisher;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically triggers outbox draining. ShedLock keeps only one active publisher in the cluster. */
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventPublisher publisher;

    @Scheduled(fixedDelayString = "${application.outbox.poll-delay-ms}")
    @SchedulerLock(
            name = "knowledge-outbox-publisher",
            lockAtMostFor = "${application.outbox.lock-at-most-for}",
            lockAtLeastFor = "${application.outbox.lock-at-least-for}"
    )
    public void publishPendingEvents() {
        publisher.publishPendingEvents();
    }
}
