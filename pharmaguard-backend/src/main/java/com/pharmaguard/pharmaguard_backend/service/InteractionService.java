
package com.pharmaguard.pharmaguard_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pharmaguard.pharmaguard_backend.analysis.InteractionResult;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

@Service
public class InteractionService {

    private final ClinicalEvidenceProvider evidenceProvider;

    public InteractionService(
            ClinicalEvidenceProvider evidenceProvider) {
        this.evidenceProvider = evidenceProvider;
    }

    public List<InteractionResult> analyze(
            List<Medicine> medicines) {

        List<InteractionResult> results = new ArrayList<>();

        for (int i = 0; i < medicines.size(); i++) {
            for (int j = i + 1; j < medicines.size(); j++) {

                Medicine firstMedicine = medicines.get(i);
                Medicine secondMedicine = medicines.get(j);

                Optional<InteractionEvidence> evidence =
                        evidenceProvider.checkInteraction(
                                firstMedicine, secondMedicine);

                if (evidence.isPresent()) {
                    InteractionEvidence item = evidence.get();

                    results.add(new InteractionResult(
                            firstMedicine.getId(),
                            firstMedicine.getName(),
                            secondMedicine.getId(),
                            secondMedicine.getName(),
                            item.getStatus(),
                            item.getExplanation(),
                            item.getEvidenceSource()
                    ));
                } else {
                    results.add(new InteractionResult(
                            firstMedicine.getId(),
                            firstMedicine.getName(),
                            secondMedicine.getId(),
                            secondMedicine.getName(),
                            "NOT_CHECKED",
                            "No verified interaction result is available.",
                            "No drug-interaction reference connected"
                    ));
                }
            }
        }

        return results;
    }
}
