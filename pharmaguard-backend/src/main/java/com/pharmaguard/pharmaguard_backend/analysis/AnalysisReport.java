
package com.pharmaguard.pharmaguard_backend.analysis;

import java.util.List;

import com.pharmaguard.pharmaguard_backend.illness.Illness;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

public class AnalysisReport {

    private List<Medicine> medicines;
    private List<Illness> illnesses;
    private List<SuitabilityResult> suitabilityResults;
    private List<InteractionResult> interactionResults;

    private String counterfeitStatus;
    private String message;

    public AnalysisReport() {
    }

    public AnalysisReport(
            List<Medicine> medicines,
            List<Illness> illnesses,
            List<SuitabilityResult> suitabilityResults,
            List<InteractionResult> interactionResults,
            String counterfeitStatus,
            String message) {

        this.medicines = medicines;
        this.illnesses = illnesses;
        this.suitabilityResults = suitabilityResults;
        this.interactionResults = interactionResults;
        this.counterfeitStatus = counterfeitStatus;
        this.message = message;
    }

    public List<Medicine> getMedicines() {
        return medicines;
    }

    public List<Illness> getIllnesses() {
        return illnesses;
    }

    public List<SuitabilityResult> getSuitabilityResults() {
        return suitabilityResults;
    }

    public List<InteractionResult> getInteractionResults() {
        return interactionResults;
    }

    public String getCounterfeitStatus() {
        return counterfeitStatus;
    }

    public String getMessage() {
        return message;
    }
}
