package com.medical.contracts;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<ContractEntity, Integer> {
    List<ContractEntity> findAllByPatientId(Integer patientId);
    Optional<ContractEntity> findFirstByPatientIdAndStatusOrderByCreatedAtDesc(Integer patientId, ContractStatus status);
}
