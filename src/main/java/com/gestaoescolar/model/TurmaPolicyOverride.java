package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.EvaluationScaleType;
import jakarta.persistence.*;

@Entity
@Table(name = "turma_policy_override",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_turma_override_unique", columnNames = {"turma_id"})
        })
public class TurmaPolicyOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false, unique = true)
    private Turma turma;

    // Se null, herda do Ano Letivo. Se definido, força a escala da turma.
    @Enumerated(EnumType.STRING)
    private EvaluationScaleType evaluationScaleType;

    // Usado quando escala é CONCEITUAL
    @Column(length = 1000)
    private String conceptLabels;

    public Long getId() {
        return id;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public EvaluationScaleType getEvaluationScaleType() {
        return evaluationScaleType;
    }

    public void setEvaluationScaleType(EvaluationScaleType evaluationScaleType) {
        this.evaluationScaleType = evaluationScaleType;
    }

    public String getConceptLabels() {
        return conceptLabels;
    }

    public void setConceptLabels(String conceptLabels) {
        this.conceptLabels = conceptLabels;
    }
}
