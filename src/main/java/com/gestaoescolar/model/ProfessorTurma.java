package com.gestaoescolar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "professor_turma", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"professor_id", "turma_id"})
})
public class ProfessorTurma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    @NotNull(message = "Professor é obrigatório")
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    @NotNull(message = "Turma é obrigatória")
    private Turma turma;

    @Column(length = 50)
    private String papel;

    @Column(length = 100)
    private String disciplina;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_termino")
    private LocalDate dataTermino;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    // Construtores
    public ProfessorTurma() {}

    public ProfessorTurma(Professor professor, Turma turma) {
        this.professor = professor;
        this.turma = turma;
        this.dataInicio = LocalDate.now();
    }

    public ProfessorTurma(Professor professor, Turma turma, String papel, String disciplina) {
        this.professor = professor;
        this.turma = turma;
        this.papel = papel;
        this.disciplina = disciplina;
        this.dataInicio = LocalDate.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataTermino() {
        return dataTermino;
    }

    public void setDataTermino(LocalDate dataTermino) {
        this.dataTermino = dataTermino;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    // Métodos de negócio
    public boolean isAtivo() {
        return dataTermino == null || dataTermino.isAfter(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfessorTurma that = (ProfessorTurma) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProfessorTurma{" +
                "professor=" + (professor != null ? professor.getNomeCompleto() : "null") +
                ", turma=" + (turma != null ? turma.getCodigo() : "null") +
                ", disciplina='" + disciplina + '\'' +
                '}';
    }
}
