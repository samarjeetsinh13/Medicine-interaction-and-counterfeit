
package com.pharmaguard.pharmaguard_backend.medicine;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.pharmaguard.pharmaguard_backend.service.DrugReferenceService;
import com.pharmaguard.pharmaguard_backend.service.DrugSearchResult;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineRepository medicineRepository;
    private final DrugReferenceService drugReferenceService;

    public MedicineController(
        MedicineRepository medicineRepository,
        DrugReferenceService drugReferenceService) {
            
        this.medicineRepository = medicineRepository;
        this.drugReferenceService = drugReferenceService;
    }

    @PostMapping("/test")
    public Medicine testMedicine(@RequestBody Medicine medicine) {
        return medicine;
    }

    @PostMapping
    public ResponseEntity<?> addMedicines(
            @RequestBody MedicineRequest request) {

        if (request == null
                || request.getMedicines() == null
                || request.getMedicines().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please provide at least one medicine.");
        }

        List<Medicine> medicines = request.getMedicines();

        for (Medicine medicine : medicines) {
            if (medicine == null
                    || medicine.getName() == null
                    || medicine.getName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Every medicine must have a name.");
            }
        }

        List<Medicine> savedMedicines =
                medicineRepository.saveAll(medicines);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedMedicines);
    }

    @GetMapping
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }


    @PatchMapping("/{id}/rxnorm")
    public ResponseEntity<?> updateRxCui(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        if (request == null
                || request.get("rxCui") == null
                || request.get("rxCui").isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Please provide a valid rxCui.");
        }

        String requestedRxCui = request.get("rxCui").trim();

        if (!requestedRxCui.matches("\\d+")) {
            return ResponseEntity.badRequest()
                    .body("rxCui must be a numeric RxNorm identifier.");
        }

        var optionalMedicine = medicineRepository.findById(id);

        if (optionalMedicine.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Medicine not found.");
        }

        Medicine medicine = optionalMedicine.get();

        try {
            DrugSearchResult result =
                    drugReferenceService.findDrugConcepts(
                            medicine.getIngredient());

            boolean validCandidate = result != null
                    && result.getCandidates() != null
                    && result.getCandidates().stream()
                        .filter(candidate -> candidate != null)
                        .anyMatch(candidate -> requestedRxCui.equals(candidate.getRxcui()));

            if (!validCandidate) {
                return ResponseEntity.badRequest()
                        .body("The RxCUI was not found among the "
                                + "RxNorm candidates for this medicine.");
            }

            medicine.setRxCui(requestedRxCui);

            return ResponseEntity.ok(
                    medicineRepository.save(medicine));

        } catch (RestClientException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Unable to validate the RxCUI with RxNorm.");
        }
    }

}
