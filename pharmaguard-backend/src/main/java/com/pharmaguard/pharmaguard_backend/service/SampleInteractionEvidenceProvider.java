
package com.pharmaguard.pharmaguard_backend.service;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.pharmaguard.pharmaguard_backend.illness.Illness;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

@Service
@Profile("sample-interactions")
public class SampleInteractionEvidenceProvider
        implements ClinicalEvidenceProvider {

    @Override
    public Optional<SuitabilityEvidence> checkSuitability(
            Medicine medicine,
            Illness illness) {

        // This test provider has no suitability data.
        return Optional.empty();
    }

    @Override
    public Optional<InteractionEvidence> checkInteraction(
            Medicine firstMedicine,
            Medicine secondMedicine) {

        // Deliberately return no clinical findings.
        // Real interaction evidence will be integrated later.
        return Optional.empty();
    }
}

    