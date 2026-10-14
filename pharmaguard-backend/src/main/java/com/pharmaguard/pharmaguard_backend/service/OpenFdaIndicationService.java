
package com.pharmaguard.pharmaguard_backend.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pharmaguard.pharmaguard_backend.illness.Illness;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class OpenFdaIndicationService {

    private static final String API_BASE =
            "https://api.fda.gov/drug/label.json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenFdaIndicationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public Optional<SuitabilityEvidence> findIndication(
            Medicine medicine,
            Illness illness) {

        if (medicine == null || illness == null) {
            return Optional.empty();
        }

        String ingredient = normalize(medicine.getIngredient());
        String illnessName = normalize(illness.getName());

        if (ingredient.isBlank() || illnessName.isBlank()) {
            return Optional.empty();
        }

        try {
            String query = "openfda.generic_name:"
                    + "\""
                    + escapeSearchTerm(ingredient)
                    + "\"";

            String encodedQuery = URLEncoder.encode(
                    query, StandardCharsets.UTF_8);

            String url = API_BASE
                    + "?search=" + encodedQuery
                    + "&limit=10";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode records = root.path("results");

            if (!records.isArray()) {
                return Optional.empty();
            }

            for (JsonNode record : records) {
                JsonNode indications =
                        record.path("indications_and_usage");

                if (!indications.isArray()) {
                    continue;
                }

                for (JsonNode indicationNode : indications) {
                    String indicationText = indicationNode.asText("");

                    if (containsIllnessPhrase(
                            indicationText, illnessName)) {

                        return Optional.of(new SuitabilityEvidence(
                                "INDICATION_MATCH",
                                "The illness wording was found in an "
                                        + "openFDA medicine-label indication. "
                                        + "This is evidence of a labelled use, "
                                        + "not confirmation that the medicine "
                                        + "is suitable for this individual.",
                                url
                        ));
                    }
                }
            }

            // No matching text does not prove the medicine is unsuitable.
            return Optional.empty();

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();

        } catch (IOException | RuntimeException exception) {
            // Network or parsing failures must not produce a positive result.
            return Optional.empty();
        }
    }

    private boolean containsIllnessPhrase(
            String indicationText,
            String illnessName) {

        String normalizedText = normalize(indicationText);

        // Require a complete phrase rather than a partial word match.
        String paddedText = " " + normalizedText + " ";
        String paddedIllness = " " + illnessName + " ";

        return paddedText.contains(paddedIllness);
    }

    private String escapeSearchTerm(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
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
