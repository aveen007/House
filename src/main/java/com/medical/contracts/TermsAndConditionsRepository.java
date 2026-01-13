package com.medical.contracts;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditionsEntity, Integer> {
    List<TermsAndConditionsEntity> findAllByIsActive(Boolean isActive);
    Optional<TermsAndConditionsEntity> findByVersion(Integer version);
}
