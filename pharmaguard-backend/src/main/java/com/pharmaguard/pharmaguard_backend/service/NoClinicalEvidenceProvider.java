
package com.pharmaguard.pharmaguard_backend.service;

import java.util.Optional;

import com.pharmaguard.pharmaguard_backend.illness.Illness;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

public class NoClinicalEvidenceProvider
        implements ClinicalEvidenceProvider {

    @Override
    public Optional<SuitabilityEvidence> checkSuitability(
            Medicine medicine,
            Illness illness) {
        return Optional.empty();
    }

    @Override
    public Optional<InteractionEvidence> checkInteraction(
            Medicine firstMedicine,
            Medicine secondMedicine) {
        return Optional.empty();
    }
}
