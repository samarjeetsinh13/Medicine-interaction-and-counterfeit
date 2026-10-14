
package com.pharmaguard.pharmaguard_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pharmaguard.pharmaguard_backend.analysis.SuitabilityResult;
import com.pharmaguard.pharmaguard_backend.illness.Illness;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

@Service
public class SuitabilityService {

    private final ClinicalEvidenceProvider evidenceProvider;

    public SuitabilityService(
            ClinicalEvidenceProvider evidenceProvider) {
        this.evidenceProvider = evidenceProvider;
    }

    public List<SuitabilityResult> analyze(
            List<Medicine> medicines,
            List<Illness> illnesses) {

        List<SuitabilityResult> results = new ArrayList<>();

        for (Medicine medicine : medicines) {
            for (Illness illness : illnesses) {

                Optional<SuitabilityEvidence> evidence =
                        evidenceProvider.checkSuitability(
                                medicine, illness);

                if (evidence.isPresent()) {
                    SuitabilityEvidence item = evidence.get();

                    results.add(new SuitabilityResult(
                            medicine.getId(),
                            medicine.getName(),
                            illness.getId(),
                            illness.getName(),
                            item.getStatus(),
                            item.getExplanation(),
                            item.getEvidenceSource()
                    ));
                } else {
                    results.add(new SuitabilityResult(
                            medicine.getId(),
                            medicine.getName(),
                            illness.getId(),
                            illness.getName(),
                            "NOT_CHECKED",
                            "No verified clinical suitability result is available.",
                            "No clinical reference connected"
                    ));
                }
            }
        }

        return results;
    }
}
