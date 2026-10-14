
package com.pharmaguard.pharmaguard_backend.service;

public class InteractionEvidence {

    private final String status;
    private final String explanation;
    private final String evidenceSource;

    public InteractionEvidence(
            String status,
            String explanation,
            String evidenceSource
    ) {
        this.status = status;
        this.explanation = explanation;
        this.evidenceSource = evidenceSource;
    }

    public String getStatus() {
        return status;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getEvidenceSource() {
        return evidenceSource;
    }
}
