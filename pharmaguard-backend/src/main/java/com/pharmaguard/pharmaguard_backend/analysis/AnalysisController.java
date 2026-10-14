
package com.pharmaguard.pharmaguard_backend.analysis;

import java.util.HashSet;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmaguard.pharmaguard_backend.illness.Illness;
import com.pharmaguard.pharmaguard_backend.illness.IllnessRepository;
import com.pharmaguard.pharmaguard_backend.medicine.Medicine;
import com.pharmaguard.pharmaguard_backend.medicine.MedicineRepository;
import com.pharmaguard.pharmaguard_backend.service.InteractionService;
import com.pharmaguard.pharmaguard_backend.service.SuitabilityService;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final MedicineRepository medicineRepository;
    private final IllnessRepository illnessRepository;
    private final SuitabilityService suitabilityService;
    private final InteractionService interactionService;

    public AnalysisController(
            MedicineRepository medicineRepository,
            IllnessRepository illnessRepository,
            SuitabilityService suitabilityService,
            InteractionService interactionService) {

        this.medicineRepository = medicineRepository;
        this.illnessRepository = illnessRepository;
        this.suitabilityService = suitabilityService;
        this.interactionService = interactionService;
    }

    @PostMapping
    public ResponseEntity<?> generateReport(
            @RequestBody AnalysisRequest request) {

        if (request == null
                || request.getMedicineIds() == null
                || request.getMedicineIds().isEmpty()
                || request.getIllnessIds() == null
                || request.getIllnessIds().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Provide at least one medicine ID and one illness ID.");
        }

        if (request.getMedicineIds().contains(null)
                || request.getIllnessIds().contains(null)) {

            return ResponseEntity.badRequest()
                    .body("IDs cannot be null.");
        }

        if (new HashSet<>(request.getMedicineIds()).size()
                    != request.getMedicineIds().size()
                || new HashSet<>(request.getIllnessIds()).size()
                    != request.getIllnessIds().size()) {

            return ResponseEntity.badRequest()
                    .body("Duplicate IDs are not allowed.");
        }

        List<Medicine> medicines =
                medicineRepository.findAllById(request.getMedicineIds());

        List<Illness> illnesses =
                illnessRepository.findAllById(request.getIllnessIds());

        if (medicines.size() != request.getMedicineIds().size()
                || illnesses.size() != request.getIllnessIds().size()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("One or more medicine or illness IDs were not found.");
        }

        List<SuitabilityResult> suitabilityResults =
                suitabilityService.analyze(medicines, illnesses);

        List<InteractionResult> interactionResults =
                interactionService.analyze(medicines);

        AnalysisReport report = new AnalysisReport(
                medicines,
                illnesses,
                suitabilityResults,
                interactionResults,
                "NOT_CHECKED",
                "Suitability and interaction combinations have been generated, "
                        + "but clinical evidence checks are not connected yet. "
                        + "Counterfeit verification has not been performed."
        );

        return ResponseEntity.ok(report);
    }
}
