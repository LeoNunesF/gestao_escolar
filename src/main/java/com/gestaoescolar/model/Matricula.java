package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.MatriculaStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "matricula",
        indexes = {
                @Index(name = "idx_matricula_aluno", columnList = "aluno_id"),
                @Index(name = "idx_matricula_turma", columnList = "turma_id"),
                @Index(name = "idx_matricula_status", columnList = "status")
        })
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Aluno aluno;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Turma turma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatriculaStatus status = MatriculaStatus.ATIVA;

    @Column(nullable = false)
    private LocalDate dataInicio;

    private LocalDate dataTermino;

    @Column(length = 1000)
    private String motivo; // para cancelamento/transferência (opcional)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public MatriculaStatus getStatus() {
        return status;
    }

    public void setStatus(MatriculaStatus status) {
        this.status = status;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}