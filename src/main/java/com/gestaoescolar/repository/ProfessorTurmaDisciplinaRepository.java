package com.gestaoescolar.repository;

import com.gestaoescolar.model.ProfessorTurmaDisciplina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessorTurmaDisciplinaRepository extends JpaRepository<ProfessorTurmaDisciplina, Long> {

    @EntityGraph(attributePaths = {"professor", "turmaDisciplina", "turmaDisciplina.disciplina"})
    List<ProfessorTurmaDisciplina> findByTurmaDisciplinaId(Long turmaDisciplinaId);

    @EntityGraph(attributePaths = {"professor", "turmaDisciplina", "turmaDisciplina.turma"})
    List<ProfessorTurmaDisciplina> findByProfessorId(Long professorId);
}