package com.gestaoescolar.repository;

import com.gestaoescolar.model.AcademicPolicy;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicPolicyRepository extends JpaRepository<AcademicPolicy, Long> {

    @EntityGraph(attributePaths = {"periods"})
    Optional<AcademicPolicy> findByAnoLetivoId(Long anoLetivoId);
}