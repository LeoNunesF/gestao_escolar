package com.gestaoescolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "turma_disciplina",
        indexes = {
                @Index(name = "idx_td_turma", columnList = "turma_id"),
                @Index(name = "idx_td_disciplina", columnList = "disciplina_id")
        })
public class TurmaDisciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Turma turma;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Disciplina disciplina;

    // carga horaria específica para esta oferta (opcional)
    private Integer cargaHoraria;

    @Column(nullable = false)
    private boolean ativa = true;

    public Long getId() {
        return id;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
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

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}