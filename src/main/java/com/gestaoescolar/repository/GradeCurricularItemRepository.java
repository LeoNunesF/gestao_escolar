package com.gestaoescolar.repository;

import com.gestaoescolar.model.GradeCurricularItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeCurricularItemRepository extends JpaRepository<GradeCurricularItem, Long> {

    @EntityGraph(attributePaths = {"disciplina"})
    List<GradeCurricularItem> findByGradeId(Long gradeId);

    boolean existsByGradeIdAndDisciplinaId(Long gradeId, Long disciplinaId);
}