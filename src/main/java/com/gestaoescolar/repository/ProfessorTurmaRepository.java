package com.gestaoescolar.repository;

import com.gestaoescolar.model.ProfessorTurma;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProfessorTurmaRepository extends JpaRepository<ProfessorTurma, Long> {
    List<ProfessorTurma> findByProfessorId(Long professorId);
    List<ProfessorTurma> findByTurmaId(Long turmaId);
    Optional<ProfessorTurma> findByProfessorIdAndTurmaId(Long professorId, Long turmaId);
}
