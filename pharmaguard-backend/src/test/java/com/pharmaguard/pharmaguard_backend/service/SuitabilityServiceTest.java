
package com.pharmaguard.pharmaguard_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pharmaguard.pharmaguard_backend.analysis.SuitabilityResult;
import com.pharmaguard.pharmaguard_backend.illness.Illness;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

class SuitabilityServiceTest {

    @Test
    void shouldReturnNotCheckedWhenEvidenceIsUnavailable() {
        ClinicalEvidenceProvider provider =
                mock(ClinicalEvidenceProvider.class);

        when(provider.checkSuitability(any(), any()))
                .thenReturn(Optional.empty());

        SuitabilityService service =
                new SuitabilityService(provider);

        List<Medicine> medicines = List.of(
                new Medicine("Paracetamol", "Paracetamol",
                        "BATCH123", "Example Pharma"),
                new Medicine("Cetirizine", "Cetirizine",
                        "BATCH456", "Sample Pharma")
        );

        List<Illness> illnesses = List.of(
                new Illness("Fever", "High body temperature"),
                new Illness("Allergic rhinitis", "Sneezing")
        );

        List<SuitabilityResult> results =
                service.analyze(medicines, illnesses);

        assertEquals(4, results.size());

        for (SuitabilityResult result : results) {
            assertEquals("NOT_CHECKED", result.getStatus());
        }

        verify(provider, times(4))
                .checkSuitability(any(), any());
    }
}
