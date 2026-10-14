
package com.pharmaguard.pharmaguard_backend.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DrugReferenceService {

    private final RestClient restClient;

    public DrugReferenceService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://rxnav.nlm.nih.gov/REST")
                .build();
    }

    public DrugSearchResult findDrugConcepts(String drugName) {

        if (drugName == null || drugName.isBlank()) {
            throw new IllegalArgumentException(
                    "Drug name must not be empty."
            );
        }

        DrugLookupResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/approximateTerm.json")
                        .queryParam("term", drugName.trim())
                        .queryParam("maxEntries", 5)
                        .build())
                .retrieve()
                .body(DrugLookupResponse.class);

        Map<String, DrugCandidate> uniqueCandidates =
                new LinkedHashMap<>();

        if (response != null
                && response.getApproximateGroup() != null
                && response.getApproximateGroup().getCandidate() != null) {

            for (DrugCandidate candidate :
                    response.getApproximateGroup().getCandidate()) {

                if (candidate == null
                        || candidate.getRxcui() == null
                        || candidate.getRxcui().isBlank()) {
                    continue;
                }

                String rxcui = candidate.getRxcui();
                DrugCandidate existing =
                        uniqueCandidates.get(rxcui);

                if (existing == null) {
                    uniqueCandidates.put(rxcui, candidate);
                } else if (
                        (existing.getName() == null
                                || existing.getName().isBlank())
                        && candidate.getName() != null
                        && !candidate.getName().isBlank()) {

                    // Prefer the candidate that has a name.
                    uniqueCandidates.put(rxcui, candidate);
                }
            }
        }

        List<DrugCandidate> candidates =
                new ArrayList<>(uniqueCandidates.values());

        return new DrugSearchResult(
                drugName.trim(),
                candidates
        );
    }
}
