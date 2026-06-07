package com.supporthub.knowledge.draft.application;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically drives draft body generation. ShedLock keeps a single active generator in the
 * cluster, which (together with the per-draft status guard) prevents double generation.
 */
@Component
@RequiredArgsConstructor
public class DraftContentGenerationScheduler {

    private final DraftContentGenerationService generationService;

    @Scheduled(fixedDelayString = "${application.draft-generation.poll-delay-ms}")
    @SchedulerLock(
            name = "knowledge-draft-content-generation",
            lockAtMostFor = "${application.draft-generation.lock-at-most-for}",
            lockAtLeastFor = "${application.draft-generation.lock-at-least-for}"
    )
    public void generatePendingDrafts() {
        generationService.generatePendingDrafts();
    }
}
