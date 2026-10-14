
package com.pharmaguard.pharmaguard_backend.illness;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/illnesses")
public class IllnessController {

    private final IllnessRepository illnessRepository;

    public IllnessController(IllnessRepository illnessRepository) {
        this.illnessRepository = illnessRepository;
    }

    @PostMapping
    public ResponseEntity<?> addIllnesses(
            @RequestBody IllnessRequest request) {

        if (request == null
                || request.getIllnesses() == null
                || request.getIllnesses().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please provide at least one illness.");
        }

        List<Illness> illnesses = request.getIllnesses();

        for (Illness illness : illnesses) {
            if (illness == null
                    || illness.getName() == null
                    || illness.getName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Every illness must have a name.");
            }
        }

        List<Illness> savedIllnesses =
                illnessRepository.saveAll(illnesses);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedIllnesses);
    }

    @GetMapping
    public List<Illness> getAllIllnesses() {
        return illnessRepository.findAll();
    }
}
