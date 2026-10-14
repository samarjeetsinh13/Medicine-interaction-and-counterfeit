
package com.pharmaguard.pharmaguard_backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/drug-reference")
public class DrugReferenceController {

    private final DrugReferenceService drugReferenceService;

    public DrugReferenceController(
            DrugReferenceService drugReferenceService) {
        this.drugReferenceService = drugReferenceService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDrug(
            @RequestParam String name) {

        if (name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Drug name must not be empty.");
        }

        try {
            DrugSearchResult result =
                    drugReferenceService.findDrugConcepts(name);

            return ResponseEntity.ok(result);

        } catch (RestClientException exception) {
            return ResponseEntity.status(
                    HttpStatus.BAD_GATEWAY)
                    .body(
                        "The external drug-reference service is unavailable."
                    );
        }
    }
}
