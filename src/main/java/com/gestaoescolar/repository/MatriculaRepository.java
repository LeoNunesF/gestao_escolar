package com.gestaoescolar.repository;

import com.gestaoescolar.model.Matricula;
import com.gestaoescolar.model.enums.MatriculaStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    long countByTurmaIdAndStatus(Long turmaId, MatriculaStatus status);

    boolean existsByAlunoIdAndTurmaIdAndStatus(Long alunoId, Long turmaId, MatriculaStatus status);

    @EntityGraph(attributePaths = {"aluno", "turma"})
    List<Matricula> findByTurmaId(Long turmaId);

    @EntityGraph(attributePaths = {"aluno", "turma"})
    List<Matricula> findByAlunoId(Long alunoId);

    Optional<Matricula> findByIdAndStatus(Long id, MatriculaStatus status);

    // NOVOS MÉTODOS: bloqueio por ano letivo
    boolean existsByAlunoIdAndTurma_AnoLetivo_IdAndStatus(Long alunoId, Long anoLetivoId, MatriculaStatus status);

    @EntityGraph(attributePaths = {"turma"})
    Optional<Matricula> findFirstByAlunoIdAndTurma_AnoLetivo_IdAndStatusOrderByIdDesc(Long alunoId, Long anoLetivoId, MatriculaStatus status);
}