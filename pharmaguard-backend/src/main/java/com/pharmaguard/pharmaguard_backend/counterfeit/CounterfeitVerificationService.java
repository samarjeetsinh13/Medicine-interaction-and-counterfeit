
package com.pharmaguard.pharmaguard_backend.counterfeit;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CounterfeitVerificationService {

    private final TrustedBatchRecordRepository repository;

    public CounterfeitVerificationService(
            TrustedBatchRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public CounterfeitVerificationResponse verify(
            CounterfeitVerificationRequest request) {

        String medicine = clean(request.medicineName());
        String batch = clean(request.batchNumber());
        String manufacturer = clean(request.manufacturer());

        List<TrustedBatchRecord> records =
                repository.findByMedicineNameIgnoreCaseAndBatchNumberIgnoreCase(
                        medicine, batch);

        for (TrustedBatchRecord record : records) {
            if (!record.isActive()) {
                continue;
            }

            boolean sameManufacturer =
                    normalize(record.getManufacturer())
                    .equals(normalize(manufacturer));

            boolean hasSource =
                    record.getEvidenceSource() != null
                    && !record.getEvidenceSource().isBlank();

            if (sameManufacturer && hasSource) {
                return new CounterfeitVerificationResponse(
                        medicine,
                        batch,
                        manufacturer,
                        "VERIFIED",
                        "The submitted details match an active trusted record. This is a database match, not a guarantee against counterfeiting.",
                        record.getEvidenceSource());
            }
        }

        boolean batchExists = records.stream()
                .anyMatch(TrustedBatchRecord::isActive);

        if (batchExists) {
            return new CounterfeitVerificationResponse(
                    medicine,
                    batch,
                    manufacturer,
                    "FLAGGED",
                    "The manufacturer does not match the active batch record. Further verification is required; this does not prove counterfeiting.",
                    "Trusted batch database");
        }

        return new CounterfeitVerificationResponse(
                medicine,
                batch,
                manufacturer,
                "UNVERIFIED",
                "No active trusted record was found. Authenticity could not be confirmed.",
                "No matching trusted record");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
