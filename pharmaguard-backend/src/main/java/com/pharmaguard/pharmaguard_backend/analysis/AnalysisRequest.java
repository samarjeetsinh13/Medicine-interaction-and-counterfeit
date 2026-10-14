
package com.pharmaguard.pharmaguard_backend.analysis;

import java.util.List;

public class AnalysisRequest {

    private List<Long> medicineIds;
    private List<Long> illnessIds;

    public AnalysisRequest() {
    }

    public List<Long> getMedicineIds() {
        return medicineIds;
    }

    public void setMedicineIds(List<Long> medicineIds) {
        this.medicineIds = medicineIds;
    }

    public List<Long> getIllnessIds() {
        return illnessIds;
    }

    public void setIllnessIds(List<Long> illnessIds) {
        this.illnessIds = illnessIds;
    }
}
