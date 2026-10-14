package com.pharmaguard.pharmaguard_backend.medicine;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineRepository
        extends JpaRepository<Medicine, Long> {
}
