package com.gestaoescolar.repository;

import com.gestaoescolar.model.AcademicPeriod;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, Long> {

    @EntityGraph(attributePaths = {"policy"})
    List<AcademicPeriod> findByPolicyIdOrderByIndexNumberAsc(Long policyId);
}