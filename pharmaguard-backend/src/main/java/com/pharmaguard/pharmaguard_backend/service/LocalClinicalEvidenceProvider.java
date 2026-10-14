
package com.pharmaguard.pharmaguard_backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.pharmaguard.pharmaguard_backend.illness.Illness;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class LocalClinicalEvidenceProvider
        implements ClinicalEvidenceProvider {

    private final JsonNode interactions;
    private final OpenFdaIndicationService indicationService;

    public LocalClinicalEvidenceProvider(
            OpenFdaIndicationService indicationService) {

        this.indicationService = indicationService;

        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream input = new ClassPathResource(
                "data/interactions.json").getInputStream()) {

            JsonNode root = objectMapper.readTree(input);
            JsonNode interactionArray = root.path("interactions");

            if (!interactionArray.isArray()) {
                throw new IllegalStateException(
                        "interactions.json must contain an "
                                + "'interactions' array.");
            }

            this.interactions = interactionArray;

        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "Could not load or parse "
                            + "data/interactions.json",
                    exception);
        }
    }

    @Override
    public Optional<SuitabilityEvidence> checkSuitability(
            Medicine medicine,
            Illness illness) {

        if (medicine == null || illness == null) {
            return Optional.empty();
        }

        return indicationService.findIndication(medicine, illness);
    }

    @Override
    public Optional<InteractionEvidence> checkInteraction(
            Medicine firstMedicine,
            Medicine secondMedicine) {

        if (firstMedicine == null || secondMedicine == null) {
            return Optional.empty();
        }

        String first = normalize(firstMedicine.getIngredient());
        String second = normalize(secondMedicine.getIngredient());

        if (first.isBlank() || second.isBlank()) {
            return Optional.empty();
        }

        for (JsonNode record : interactions) {
            String ingredientA = normalize(
                    record.path("ingredientA").asText(""));

            String ingredientB = normalize(
                    record.path("ingredientB").asText(""));

            boolean directMatch = first.equals(ingredientA)
                    && second.equals(ingredientB);

            boolean reverseMatch = first.equals(ingredientB)
                    && second.equals(ingredientA);

            if (!directMatch && !reverseMatch) {
                continue;
            }

            String status = record.path("status").asText("").trim();
            String explanation =
                    record.path("explanation").asText("").trim();
            String source =
                    record.path("evidenceSource").asText("").trim();

            // Incomplete records must not create misleading evidence.
            if (status.isBlank()
                    || explanation.isBlank()
                    || source.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new InteractionEvidence(
                    status,
                    explanation,
                    source));
        }

        // No record means not checked, not a confirmed absence of risk.
        return Optional.empty();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
