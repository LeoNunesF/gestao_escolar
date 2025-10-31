package com.gestaoescolar.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "aluno_responsavel",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_aluno_responsavel", columnNames = {"aluno_id", "responsavel_id"})
        }
)
public class AlunoResponsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Aluno aluno;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Responsavel responsavel;

    // Papéis no vínculo
    private boolean responsavelDidatico;
    private boolean responsavelFinanceiro;
    private boolean responsavelLegal;

    @Column(nullable = false)
    private boolean ativo = true;

    // Auditoria simples
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

    // Getters e setters

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Responsavel getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Responsavel responsavel) {
        this.responsavel = responsavel;
    }

    public boolean isResponsavelDidatico() {
        return responsavelDidatico;
    }

    public void setResponsavelDidatico(boolean responsavelDidatico) {
        this.responsavelDidatico = responsavelDidatico;
    }

    public boolean isResponsavelFinanceiro() {
        return responsavelFinanceiro;
    }

    public void setResponsavelFinanceiro(boolean responsavelFinanceiro) {
        this.responsavelFinanceiro = responsavelFinanceiro;
    }

    public boolean isResponsavelLegal() {
        return responsavelLegal;
    }

    public void setResponsavelLegal(boolean responsavelLegal) {
        this.responsavelLegal = responsavelLegal;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}