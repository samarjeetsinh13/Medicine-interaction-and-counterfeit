
package com.pharmaguard.pharmaguard_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.pharmaguard.pharmaguard_backend.analysis.InteractionResult;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

class InteractionServiceTest {

    @Test
    void shouldReturnNotCheckedWhenEvidenceIsUnavailable() {
        ClinicalEvidenceProvider provider =
                mock(ClinicalEvidenceProvider.class);

        when(provider.checkInteraction(any(), any()))
                .thenReturn(Optional.empty());

        InteractionService service =
                new InteractionService(provider);

        List<Medicine> medicines = List.of(
                new Medicine("Paracetamol", "Paracetamol",
                        "BATCH123", "Example Pharma"),
                new Medicine("Cetirizine", "Cetirizine",
                        "BATCH456", "Sample Pharma")
        );

        List<InteractionResult> results =
                service.analyze(medicines);

        assertEquals(1, results.size());
        assertEquals("NOT_CHECKED", results.get(0).getStatus());

        verify(provider, times(1))
                .checkInteraction(any(), any());
    }
}
