
package com.pharmaguard.pharmaguard_backend.analysis;

public class SuitabilityResult {

    private Long medicineId;
    private String medicineName;
    private Long illnessId;
    private String illnessName;
    private String status;
    private String explanation;
    private String evidenceSource;

    public SuitabilityResult() {
    }

    public SuitabilityResult(
            Long medicineId,
            String medicineName,
            Long illnessId,
            String illnessName,
            String status,
            String explanation,
            String evidenceSource) {

        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.illnessId = illnessId;
        this.illnessName = illnessName;
        this.status = status;
        this.explanation = explanation;
        this.evidenceSource = evidenceSource;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public Long getIllnessId() {
        return illnessId;
    }

    public String getIllnessName() {
        return illnessName;
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
