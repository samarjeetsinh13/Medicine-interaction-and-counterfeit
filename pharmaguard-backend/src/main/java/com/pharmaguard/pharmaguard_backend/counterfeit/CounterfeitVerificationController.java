
package com.pharmaguard.pharmaguard_backend.counterfeit;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counterfeit")
public class CounterfeitVerificationController {

    private final CounterfeitVerificationService service;

    public CounterfeitVerificationController(
            CounterfeitVerificationService service) {
        this.service = service;
    }

    @PostMapping("/verify")
    public ResponseEntity<CounterfeitVerificationResponse> verify(
            @Valid @RequestBody CounterfeitVerificationRequest request) {

        return ResponseEntity.ok(service.verify(request));
    }
}
