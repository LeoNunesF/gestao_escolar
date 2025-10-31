package com.gestaoescolar.repository;

import com.gestaoescolar.model.AlunoResponsavel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoResponsavelRepository extends JpaRepository<AlunoResponsavel, Long> {

    // Evita LazyInitialization na Grid de responsáveis (carrega Responsavel junto)
    @EntityGraph(attributePaths = {"responsavel"})
    List<AlunoResponsavel> findByAlunoIdAndAtivoTrue(Long alunoId);

    Optional<AlunoResponsavel> findByAlunoIdAndResponsavelId(Long alunoId, Long responsavelId);
}