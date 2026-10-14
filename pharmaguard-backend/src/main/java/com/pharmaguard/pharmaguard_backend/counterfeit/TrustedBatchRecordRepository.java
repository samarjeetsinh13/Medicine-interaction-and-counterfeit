
package com.pharmaguard.pharmaguard_backend.counterfeit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrustedBatchRecordRepository
        extends JpaRepository<TrustedBatchRecord, Long> {

    List<TrustedBatchRecord>
    findByMedicineNameIgnoreCaseAndBatchNumberIgnoreCase(
            String medicineName,
            String batchNumber);
}
