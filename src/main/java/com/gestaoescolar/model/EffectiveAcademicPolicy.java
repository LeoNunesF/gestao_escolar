package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.AcademicRoundingMode;
import com.gestaoescolar.model.enums.EvaluationScaleType;
import com.gestaoescolar.model.enums.EvaluationWeightingMode;
import com.gestaoescolar.model.enums.PeriodType;
import com.gestaoescolar.model.enums.RecoveryRule;

public class EffectiveAcademicPolicy {

    // Escala efetiva
    private EvaluationScaleType evaluationScaleType;
    private String conceptLabels;

    // Periodicidade e períodos
    private PeriodType periodType;
    private Integer totalPeriods;

    // Configurações numéricas (usadas se escala NUMÉRICA)
    private Double scaleMin;
    private Double scaleMax;
    private Integer decimalPrecision;
    private AcademicRoundingMode roundingMode;
    private Double minAverageForApproval;
    private EvaluationWeightingMode evaluationWeighting;
    private Double totalPointsPerPeriod;
    private RecoveryRule recoveryRule;

    // Frequência mínima
    private Integer minAttendancePercent;

    public static EffectiveAcademicPolicy from(AcademicPolicy base, TurmaPolicyOverride override) {
        EffectiveAcademicPolicy eff = new EffectiveAcademicPolicy();
        // Começa com a base do ano
        eff.evaluationScaleType = base.getEvaluationScaleType();
        eff.conceptLabels = base.getConceptLabels();

        eff.periodType = base.getPeriodType();
        eff.totalPeriods = base.getTotalPeriods();

        eff.scaleMin = base.getScaleMin();
        eff.scaleMax = base.getScaleMax();
        eff.decimalPrecision = base.getDecimalPrecision();
        eff.roundingMode = base.getRoundingMode();
        eff.minAverageForApproval = base.getMinAverageForApproval();
        eff.evaluationWeighting = base.getEvaluationWeighting();
        eff.totalPointsPerPeriod = base.getTotalPointsPerPeriod();
        eff.recoveryRule = base.getRecoveryRule();

        eff.minAttendancePercent = base.getMinAttendancePercent();

        // Aplica override se existir
        if (override != null) {
            if (override.getEvaluationScaleType() != null) {
                eff.evaluationScaleType = override.getEvaluationScaleType();
            }
            // Se efetiva for CONCEITUAL, usa conceitos do override se informados
            if (eff.evaluationScaleType == EvaluationScaleType.CONCEITUAL) {
                if (override.getConceptLabels() != null && !override.getConceptLabels().isBlank()) {
                    eff.conceptLabels = override.getConceptLabels();
                }
            }
        }
        return eff;
    }

    public EvaluationScaleType getEvaluationScaleType() { return evaluationScaleType; }
    public String getConceptLabels() { return conceptLabels; }
    public PeriodType getPeriodType() { return periodType; }
    public Integer getTotalPeriods() { return totalPeriods; }
    public Double getScaleMin() { return scaleMin; }
    public Double getScaleMax() { return scaleMax; }
    public Integer getDecimalPrecision() { return decimalPrecision; }
    public AcademicRoundingMode getRoundingMode() { return roundingMode; }
    public Double getMinAverageForApproval() { return minAverageForApproval; }
    public EvaluationWeightingMode getEvaluationWeighting() { return evaluationWeighting; }
    public Double getTotalPointsPerPeriod() { return totalPointsPerPeriod; }
    public RecoveryRule getRecoveryRule() { return recoveryRule; }
    public Integer getMinAttendancePercent() { return minAttendancePercent; }
}