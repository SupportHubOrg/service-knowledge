package com.supporthub.knowledge.draft.persistence;

import com.supporthub.knowledge.draft.domain.DraftStatus;

import java.util.UUID;

public class IllegalDraftStateException extends RuntimeException {

    public IllegalDraftStateException(UUID draftId, DraftStatus status, String operation) {
        super("Draft " + draftId + " with status " + status + " cannot be used for operation " + operation);
    }
}
