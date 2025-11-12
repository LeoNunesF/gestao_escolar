package com.gestaoescolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "grade_curricular_item",
        indexes = {
                @Index(name = "idx_gi_grade", columnList = "grade_id"),
                @Index(name = "idx_gi_disciplina", columnList = "disciplina_id")
        })
public class GradeCurricularItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private GradeCurricular grade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Disciplina disciplina;

    // carga por disciplina na grade (opcional)
    private Integer cargaHoraria;

    public Long getId() {
        return id;
    }

    public GradeCurricular getGrade() {
        return grade;
    }

    public void setGrade(GradeCurricular grade) {
        this.grade = grade;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }
}