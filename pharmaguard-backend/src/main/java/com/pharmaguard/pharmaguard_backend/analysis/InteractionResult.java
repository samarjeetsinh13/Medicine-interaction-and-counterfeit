
package com.pharmaguard.pharmaguard_backend.analysis;

public class InteractionResult {

    private Long firstMedicineId;
    private String firstMedicineName;
    private Long secondMedicineId;
    private String secondMedicineName;
    private String status;
    private String explanation;
    private String evidenceSource;

    public InteractionResult() {
    }

    public InteractionResult(
            Long firstMedicineId,
            String firstMedicineName,
            Long secondMedicineId,
            String secondMedicineName,
            String status,
            String explanation,
            String evidenceSource) {

        this.firstMedicineId = firstMedicineId;
        this.firstMedicineName = firstMedicineName;
        this.secondMedicineId = secondMedicineId;
        this.secondMedicineName = secondMedicineName;
        this.status = status;
        this.explanation = explanation;
        this.evidenceSource = evidenceSource;
    }

    public Long getFirstMedicineId() {
        return firstMedicineId;
    }

    public String getFirstMedicineName() {
        return firstMedicineName;
    }

    public Long getSecondMedicineId() {
        return secondMedicineId;
    }

    public String getSecondMedicineName() {
        return secondMedicineName;
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
