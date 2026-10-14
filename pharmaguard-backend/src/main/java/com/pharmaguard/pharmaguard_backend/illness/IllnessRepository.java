
package com.pharmaguard.pharmaguard_backend.illness;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IllnessRepository
        extends JpaRepository<Illness, Long> {
}
