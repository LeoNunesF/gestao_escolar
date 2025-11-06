package com.gestaoescolar.repository;

import com.gestaoescolar.model.TurmaDisciplina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurmaDisciplinaRepository extends JpaRepository<TurmaDisciplina, Long> {

    @EntityGraph(attributePaths = {"disciplina"})
    List<TurmaDisciplina> findByTurmaId(Long turmaId);

    @EntityGraph(attributePaths = {"turma", "disciplina"})
    List<TurmaDisciplina> findByDisciplinaId(Long disciplinaId);
    // NOVO: impedir inserir disciplina repetida na mesma turma
    boolean existsByTurmaIdAndDisciplinaId(Long turmaId, Long disciplinaId);
}