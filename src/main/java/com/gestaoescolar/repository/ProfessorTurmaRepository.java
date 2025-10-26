package com.gestaoescolar.repository;

import com.gestaoescolar.dto.TurmaResumoDTO;
import com.gestaoescolar.model.ProfessorTurma;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfessorTurmaRepository extends JpaRepository<ProfessorTurma, Long> {

    @EntityGraph(attributePaths = { "turma" })
    List<ProfessorTurma> findByProfessorId(Long professorId);

    @EntityGraph(attributePaths = { "professor" })
    List<ProfessorTurma> findByTurmaId(Long turmaId);

    Optional<ProfessorTurma> findByProfessorIdAndTurmaId(Long professorId, Long turmaId);

    // NOVO: busca direta em DTO (evita qualquer lazy/proxy)
    @Query("select new com.gestaoescolar.dto.TurmaResumoDTO(t.codigo, t.nomeTurma) " +
            "from ProfessorTurma pt join pt.turma t " +
            "where pt.professor.id = :profId")
    List<TurmaResumoDTO> findTurmasResumoByProfessorId(@Param("profId") Long profId);
}