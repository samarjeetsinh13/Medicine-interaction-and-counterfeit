
package com.pharmaguard.pharmaguard_backend.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

class LocalClinicalEvidenceProviderTest {

    private LocalClinicalEvidenceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new LocalClinicalEvidenceProvider(
        Mockito.mock(OpenFdaIndicationService.class));
    }

    private Medicine medicineWithIngredient(String ingredient) {
        Medicine medicine = mock(Medicine.class);
        when(medicine.getIngredient()).thenReturn(ingredient);
        return medicine;
    }

    @Test
    void findsKnownInteraction() {
        Medicine sildenafil =
                medicineWithIngredient("sildenafil");
        Medicine nitroglycerin =
                medicineWithIngredient("nitroglycerin");

        var result = provider.checkInteraction(
                sildenafil, nitroglycerin);

        assertTrue(result.isPresent());
        assertEquals("CONTRAINDICATED",
                result.get().getStatus());
        assertTrue(result.get().getExplanation()
                .toLowerCase().contains("sildenafil"));
    }

    @Test
    void findsInteractionInReverseOrder() {
        Medicine nitroglycerin =
                medicineWithIngredient("nitroglycerin");
        Medicine sildenafil =
                medicineWithIngredient("sildenafil");

        var result = provider.checkInteraction(
                nitroglycerin, sildenafil);

        assertTrue(result.isPresent());
        assertEquals("CONTRAINDICATED",
                result.get().getStatus());
    }

    @Test
    void unknownPairDoesNotInventInteraction() {
        Medicine metformin =
                medicineWithIngredient("metformin");
        Medicine paracetamol =
                medicineWithIngredient("paracetamol");

        var result = provider.checkInteraction(
                metformin, paracetamol);

        assertTrue(result.isEmpty());
    }

    @Test
    void missingIngredientDoesNotMatch() {
        Medicine metformin =
                medicineWithIngredient("metformin");
        Medicine missingIngredient =
                medicineWithIngredient(null);

        var result = provider.checkInteraction(
                metformin, missingIngredient);

        assertTrue(result.isEmpty());
    }
}
