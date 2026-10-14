
package com.pharmaguard.pharmaguard_backend.counterfeit;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CounterfeitVerificationServiceTest {

    private TrustedBatchRecordRepository repository;
    private CounterfeitVerificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(TrustedBatchRecordRepository.class);
        service = new CounterfeitVerificationService(repository);
    }

    private CounterfeitVerificationRequest request(
            String medicine, String batch, String manufacturer) {
        return new CounterfeitVerificationRequest(
                medicine, batch, manufacturer);
    }

    private TrustedBatchRecord trustedRecord(
            String medicine, String batch, String manufacturer) {
        TrustedBatchRecord record = new TrustedBatchRecord();
        record.setMedicineName(medicine);
        record.setBatchNumber(batch);
        record.setManufacturer(manufacturer);
        record.setEvidenceSource("Test source");
        record.setActive(true);
        return record;
    }

    @Test
    void matchingTrustedRecordIsVerified() {
        when(repository
                .findByMedicineNameIgnoreCaseAndBatchNumberIgnoreCase(
                        "Metformin", "BATCH-001"))
                .thenReturn(List.of(trustedRecord(
                        "Metformin", "BATCH-001", "Example Pharma")));

        var result = service.verify(request(
                "Metformin", "BATCH-001", "Example Pharma"));

        assertEquals("VERIFIED", result.status());
    }

    @Test
    void unknownBatchIsUnverified() {
        when(repository
                .findByMedicineNameIgnoreCaseAndBatchNumberIgnoreCase(
                        "Metformin", "UNKNOWN-001"))
                .thenReturn(List.of());

        var result = service.verify(request(
                "Metformin", "UNKNOWN-001", "Example Pharma"));

        assertEquals("UNVERIFIED", result.status());
    }

    @Test
    void manufacturerMismatchIsFlagged() {
        when(repository
                .findByMedicineNameIgnoreCaseAndBatchNumberIgnoreCase(
                        "Metformin", "BATCH-001"))
                .thenReturn(List.of(trustedRecord(
                        "Metformin", "BATCH-001", "Example Pharma")));

        var result = service.verify(request(
                "Metformin", "BATCH-001", "Different Pharma"));

        assertEquals("FLAGGED", result.status());
    }

    @Test
    void matchingIsCaseInsensitiveAndTrimsInput() {
        when(repository
                .findByMedicineNameIgnoreCaseAndBatchNumberIgnoreCase(
                        "metformin", "batch-001"))
                .thenReturn(List.of(trustedRecord(
                        "Metformin", "BATCH-001", "Example Pharma")));

        var result = service.verify(request(
                " metformin ", " batch-001 ", " example pharma "));

        assertEquals("VERIFIED", result.status());
    }
}
