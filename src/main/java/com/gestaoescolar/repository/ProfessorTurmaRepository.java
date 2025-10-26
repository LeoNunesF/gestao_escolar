package com.gestaoescolar.repository;

import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorTurmaRepository extends JpaRepository<ProfessorTurma, Long> {

    // Buscar todas as atribuições de um professor
    @Query("SELECT pt FROM ProfessorTurma pt " +
           "JOIN FETCH pt.turma t " +
           "WHERE pt.professor.id = :professorId " +
           "ORDER BY t.codigo")
    List<ProfessorTurma> findByProfessorId(@Param("professorId") Long professorId);

    // Buscar todos os professores de uma turma
    @Query("SELECT pt FROM ProfessorTurma pt " +
           "JOIN FETCH pt.professor p " +
           "WHERE pt.turma.id = :turmaId " +
           "ORDER BY p.nomeCompleto")
    List<ProfessorTurma> findByTurmaId(@Param("turmaId") Long turmaId);

    // Verificar se já existe atribuição
    boolean existsByProfessorIdAndTurmaId(Long professorId, Long turmaId);

    // Buscar atribuição específica
    Optional<ProfessorTurma> findByProfessorIdAndTurmaId(Long professorId, Long turmaId);

    // Buscar atribuições por disciplina
    List<ProfessorTurma> findByDisciplina(String disciplina);

    // Buscar atribuições ativas (sem data de término ou data de término futura)
    @Query("SELECT pt FROM ProfessorTurma pt " +
           "WHERE pt.dataTermino IS NULL OR pt.dataTermino > CURRENT_DATE")
    List<ProfessorTurma> findAtivas();

    // Buscar atribuições ativas de um professor
    @Query("SELECT pt FROM ProfessorTurma pt " +
           "JOIN FETCH pt.turma " +
           "WHERE pt.professor.id = :professorId " +
           "AND (pt.dataTermino IS NULL OR pt.dataTermino > CURRENT_DATE)")
    List<ProfessorTurma> findAtivasByProfessorId(@Param("professorId") Long professorId);

    // Buscar atribuições ativas de uma turma
    @Query("SELECT pt FROM ProfessorTurma pt " +
           "JOIN FETCH pt.professor " +
           "WHERE pt.turma.id = :turmaId " +
           "AND (pt.dataTermino IS NULL OR pt.dataTermino > CURRENT_DATE)")
    List<ProfessorTurma> findAtivasByTurmaId(@Param("turmaId") Long turmaId);
}
