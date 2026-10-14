
package com.pharmaguard.pharmaguard_backend.counterfeit;

import jakarta.validation.constraints.NotBlank;

public record CounterfeitVerificationRequest(
        @NotBlank String medicineName,
        @NotBlank String batchNumber,
        @NotBlank String manufacturer
) {
}
