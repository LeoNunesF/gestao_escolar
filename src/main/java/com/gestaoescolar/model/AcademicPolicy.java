package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.AcademicRoundingMode;
import com.gestaoescolar.model.enums.EvaluationScaleType;
import com.gestaoescolar.model.enums.EvaluationWeightingMode;
import com.gestaoescolar.model.enums.PeriodType;
import com.gestaoescolar.model.enums.RecoveryRule;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "academic_policy",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_policy_anoletivo", columnNames = {"ano_letivo_id"})
        })
public class AcademicPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "ano_letivo_id", nullable = false, unique = true)
    private AnoLetivo anoLetivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType periodType = PeriodType.BIMESTRE;

    // número de períodos esperado (4 bimestres, 3 trimestres)
    @Column(nullable = false)
    private Integer totalPeriods = 4;

    // Escala de avaliação (numérica ou conceitual)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationScaleType evaluationScaleType = EvaluationScaleType.NUMERICA;

    // Quando CONCEITUAL: rótulos separados por ';' (ex.: "Não Avaliado; Em Desenvolvimento; Satisfatório")
    @Column(length = 1000)
    private String conceptLabels;

    // escala de notas (quando NUMÉRICA)
    @Column(nullable = false)
    private Double scaleMin = 0.0;

    @Column(nullable = false)
    private Double scaleMax = 10.0;

    // casas decimais na exibição (quando NUMÉRICA)
    @Column(nullable = false)
    private Integer decimalPrecision = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademicRoundingMode roundingMode = AcademicRoundingMode.HALF_UP;

    // aprovação (quando NUMÉRICA)
    @Column(nullable = false)
    private Double minAverageForApproval = 6.0;

    // frequência mínima (aplica-se tanto a numérica quanto a conceitual)
    @Column(nullable = false)
    private Integer minAttendancePercent = 75;

    // modelo de notas do período (quando NUMÉRICA)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationWeightingMode evaluationWeighting = EvaluationWeightingMode.BY_WEIGHT;

    // pontos totais por período (modelo por pontos — quando NUMÉRICA)
    private Double totalPointsPerPeriod = 10.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecoveryRule recoveryRule = RecoveryRule.REPLACE_PERIOD_AVG_IF_HIGHER;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("indexNumber ASC")
    private List<AcademicPeriod> periods = new ArrayList<>();

    public Long getId() { return id; }
    public AnoLetivo getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(AnoLetivo anoLetivo) { this.anoLetivo = anoLetivo; }
    public PeriodType getPeriodType() { return periodType; }
    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }
    public Integer getTotalPeriods() { return totalPeriods; }
    public void setTotalPeriods(Integer totalPeriods) { this.totalPeriods = totalPeriods; }

    public EvaluationScaleType getEvaluationScaleType() { return evaluationScaleType; }
    public void setEvaluationScaleType(EvaluationScaleType evaluationScaleType) { this.evaluationScaleType = evaluationScaleType; }

    public String getConceptLabels() { return conceptLabels; }
    public void setConceptLabels(String conceptLabels) { this.conceptLabels = conceptLabels; }

    public Double getScaleMin() { return scaleMin; }
    public void setScaleMin(Double scaleMin) { this.scaleMin = scaleMin; }
    public Double getScaleMax() { return scaleMax; }
    public void setScaleMax(Double scaleMax) { this.scaleMax = scaleMax; }
    public Integer getDecimalPrecision() { return decimalPrecision; }
    public void setDecimalPrecision(Integer decimalPrecision) { this.decimalPrecision = decimalPrecision; }
    public AcademicRoundingMode getRoundingMode() { return roundingMode; }
    public void setRoundingMode(AcademicRoundingMode roundingMode) { this.roundingMode = roundingMode; }
    public Double getMinAverageForApproval() { return minAverageForApproval; }
    public void setMinAverageForApproval(Double minAverageForApproval) { this.minAverageForApproval = minAverageForApproval; }
    public Integer getMinAttendancePercent() { return minAttendancePercent; }
    public void setMinAttendancePercent(Integer minAttendancePercent) { this.minAttendancePercent = minAttendancePercent; }
    public EvaluationWeightingMode getEvaluationWeighting() { return evaluationWeighting; }
    public void setEvaluationWeighting(EvaluationWeightingMode evaluationWeighting) { this.evaluationWeighting = evaluationWeighting; }
    public Double getTotalPointsPerPeriod() { return totalPointsPerPeriod; }
    public void setTotalPointsPerPeriod(Double totalPointsPerPeriod) { this.totalPointsPerPeriod = totalPointsPerPeriod; }
    public RecoveryRule getRecoveryRule() { return recoveryRule; }
    public void setRecoveryRule(RecoveryRule recoveryRule) { this.recoveryRule = recoveryRule; }
    public List<AcademicPeriod> getPeriods() { return periods; }
    public void setPeriods(List<AcademicPeriod> periods) { this.periods = periods; }
}