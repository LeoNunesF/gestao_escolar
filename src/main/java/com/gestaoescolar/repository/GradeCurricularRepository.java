package com.gestaoescolar.repository;

import com.gestaoescolar.model.GradeCurricular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeCurricularRepository extends JpaRepository<GradeCurricular, Long> {
    Optional<GradeCurricular> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}