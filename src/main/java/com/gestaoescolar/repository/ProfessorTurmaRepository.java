package com.gestaoescolar.repository;

import com.gestaoescolar.dto.TurmaResumoDTO;
import com.gestaoescolar.dto.VinculoProfessorTurmaDTO;
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

    // Novo: retorna diretamente DTO com código, nome da turma e papel
    @Query("select new com.gestaoescolar.dto.VinculoProfessorTurmaDTO(t.codigo, t.nomeTurma, pt.papel) " +
            "from ProfessorTurma pt join pt.turma t " +
            "where pt.professor.id = :profId")
    List<VinculoProfessorTurmaDTO> findVinculosResumoByProfessorId(@Param("profId") Long profId);
}