package com.gestaoescolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "professor_turma_disciplina",
        indexes = {
                @Index(name = "idx_ptd_professor", columnList = "professor_id"),
                @Index(name = "idx_ptd_turma_disciplina", columnList = "turma_disciplina_id")
        })
public class ProfessorTurmaDisciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Professor professor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TurmaDisciplina turmaDisciplina;

    // se true, professor titular; caso haja co-professor, marcar false nos outros
    private boolean titular = true;

    public Long getId() {
        return id;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public TurmaDisciplina getTurmaDisciplina() {
        return turmaDisciplina;
    }

    public void setTurmaDisciplina(TurmaDisciplina turmaDisciplina) {
        this.turmaDisciplina = turmaDisciplina;
    }

    public boolean isTitular() {
        return titular;
    }

    public void setTitular(boolean titular) {
        this.titular = titular;
    }
}