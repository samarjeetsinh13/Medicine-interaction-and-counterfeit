
package com.pharmaguard.pharmaguard_backend.service;

import java.util.Optional;

import com.pharmaguard.pharmaguard_backend.medicine.Medicine;
import com.pharmaguard.pharmaguard_backend.illness.Illness;

public interface ClinicalEvidenceProvider {

    Optional<SuitabilityEvidence> checkSuitability(
            Medicine medicine,
            Illness illness
    );

    Optional<InteractionEvidence> checkInteraction(
            Medicine firstMedicine,
            Medicine secondMedicine
    );
}
