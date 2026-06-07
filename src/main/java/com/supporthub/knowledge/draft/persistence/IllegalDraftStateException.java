package com.supporthub.knowledge.draft.persistence;

import com.supporthub.knowledge.draft.domain.DraftId;
import com.supporthub.knowledge.draft.domain.DraftStatus;

public class IllegalDraftStateException extends RuntimeException {

    public IllegalDraftStateException(DraftId draftId, DraftStatus status, String operation) {
        super("Draft " + draftId + " with status " + status + " cannot be used for operation " + operation);
    }
}
