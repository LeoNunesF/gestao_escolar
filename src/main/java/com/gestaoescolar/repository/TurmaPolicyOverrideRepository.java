package com.gestaoescolar.repository;

import com.gestaoescolar.model.TurmaPolicyOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TurmaPolicyOverrideRepository extends JpaRepository<TurmaPolicyOverride, Long> {
    Optional<TurmaPolicyOverride> findByTurmaId(Long turmaId);
    void deleteByTurmaId(Long turmaId);
    boolean existsByTurmaId(Long turmaId);
}