
package com.pharmaguard.pharmaguard_backend.counterfeit;

public record CounterfeitVerificationResponse(
        String medicineName,
        String batchNumber,
        String manufacturer,
        String status,
        String explanation,
        String evidenceSource
) {
}
