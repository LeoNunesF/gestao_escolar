package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.*;
import com.gestaoescolar.model.enums.EvaluationScaleType;
import com.gestaoescolar.model.enums.PeriodType;
import com.gestaoescolar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AcademicPolicyService {

    private final AcademicPolicyRepository policyRepo;
    private final AcademicPeriodRepository periodRepo;
    private final AnoLetivoRepository anoLetivoRepo;

    private final TurmaRepository turmaRepository;
    private final TurmaPolicyOverrideRepository turmaOverrideRepo;

    public AcademicPolicyService(AcademicPolicyRepository policyRepo,
                                 AcademicPeriodRepository periodRepo,
                                 AnoLetivoRepository anoLetivoRepo,
                                 TurmaRepository turmaRepository,
                                 TurmaPolicyOverrideRepository turmaOverrideRepo) {
        this.policyRepo = policyRepo;
        this.periodRepo = periodRepo;
        this.anoLetivoRepo = anoLetivoRepo;
        this.turmaRepository = turmaRepository;
        this.turmaOverrideRepo = turmaOverrideRepo;
    }

    // ===== Base (Ano Letivo) =====

    @Transactional
    public AcademicPolicy getOrCreatePolicy(Long anoLetivoId) {
        return policyRepo.findByAnoLetivoId(anoLetivoId).orElseGet(() -> {
            AnoLetivo ano = anoLetivoRepo.findById(anoLetivoId)
                    .orElseThrow(() -> new IllegalArgumentException("Ano letivo não encontrado."));
            AcademicPolicy p = new AcademicPolicy();
            p.setAnoLetivo(ano);
            p.setPeriodType(PeriodType.BIMESTRE);
            p.setTotalPeriods(4);
            p.setEvaluationScaleType(com.gestaoescolar.model.enums.EvaluationScaleType.NUMERICA);
            p.setScaleMin(0.0);
            p.setScaleMax(10.0);
            p.setDecimalPrecision(1);
            p.setMinAverageForApproval(6.0);
            p.setMinAttendancePercent(75);
            p = policyRepo.save(p);
            regenerateDefaultPeriods(p.getId());
            return p;
        });
    }

    @Transactional
    public AcademicPolicy savePolicy(AcademicPolicy updated) {
        if (updated.getAnoLetivo() == null || updated.getAnoLetivo().getId() == null) {
            throw new IllegalArgumentException("Ano letivo é obrigatório.");
        }
        AcademicPolicy current = policyRepo.findByAnoLetivoId(updated.getAnoLetivo().getId())
                .orElseThrow(() -> new IllegalArgumentException("Política não encontrada para o ano letivo."));

        current.setEvaluationScaleType(updated.getEvaluationScaleType());
        current.setConceptLabels(updated.getConceptLabels());

        current.setPeriodType(updated.getPeriodType());
        current.setTotalPeriods(updated.getTotalPeriods() != null ? updated.getTotalPeriods()
                : (updated.getPeriodType() == PeriodType.BIMESTRE ? 4 : 3));

        current.setScaleMin(updated.getScaleMin());
        current.setScaleMax(updated.getScaleMax());
        current.setDecimalPrecision(updated.getDecimalPrecision());
        current.setRoundingMode(updated.getRoundingMode());
        current.setMinAverageForApproval(updated.getMinAverageForApproval());
        current.setMinAttendancePercent(updated.getMinAttendancePercent());
        current.setEvaluationWeighting(updated.getEvaluationWeighting());
        current.setTotalPointsPerPeriod(updated.getTotalPointsPerPeriod());
        current.setRecoveryRule(updated.getRecoveryRule());

        return policyRepo.save(current);
    }

    @Transactional
    public void regenerateDefaultPeriods(Long policyId) {
        AcademicPolicy p = policyRepo.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Política acadêmica não encontrada."));
        List<AcademicPeriod> existing = periodRepo.findByPolicyIdOrderByIndexNumberAsc(policyId);
        periodRepo.deleteAll(existing);

        int n = p.getTotalPeriods() != null ? p.getTotalPeriods()
                : (p.getPeriodType() == PeriodType.BIMESTRE ? 4 : 3);
        List<AcademicPeriod> created = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            AcademicPeriod ap = new AcademicPeriod();
            ap.setPolicy(p);
            ap.setIndexNumber(i);
            String nome = (p.getPeriodType() == PeriodType.BIMESTRE ? i + "º Bimestre" : i + "º Trimestre");
            ap.setName(nome);
            created.add(ap);
        }
        periodRepo.saveAll(created);
    }

    @Transactional
    public AcademicPeriod updatePeriod(Long periodId, String name, java.time.LocalDate start, java.time.LocalDate end) {
        AcademicPeriod ap = periodRepo.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Período não encontrado."));
        if (name != null && !name.isBlank()) ap.setName(name.trim());
        ap.setStartDate(start);
        ap.setEndDate(end);
        return periodRepo.save(ap);
    }

    public List<AcademicPeriod> listPeriods(Long policyId) {
        return periodRepo.findByPolicyIdOrderByIndexNumberAsc(policyId);
    }

    // ===== Overrides por Turma =====

    public Optional<TurmaPolicyOverride> getOverrideForTurma(Long turmaId) {
        return turmaOverrideRepo.findByTurmaId(turmaId);
    }

    @Transactional
    public TurmaPolicyOverride saveTurmaOverride(Long turmaId, EvaluationScaleType scaleType, String conceptLabels) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));
        TurmaPolicyOverride ov = turmaOverrideRepo.findByTurmaId(turmaId).orElseGet(TurmaPolicyOverride::new);
        ov.setTurma(turma);
        ov.setEvaluationScaleType(scaleType);
        ov.setConceptLabels(conceptLabels);
        return turmaOverrideRepo.save(ov);
    }

    @Transactional
    public void removeTurmaOverride(Long turmaId) {
        turmaOverrideRepo.deleteByTurmaId(turmaId);
    }

    @Transactional(readOnly = true)
    public EffectiveAcademicPolicy getEffectivePolicyForTurma(Long turmaId) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));
        Long anoId = turma.getAnoLetivo() != null ? turma.getAnoLetivo().getId() : null;
        if (anoId == null) throw new IllegalArgumentException("Turma sem ano letivo associado.");

        AcademicPolicy base = getOrCreatePolicy(anoId);
        TurmaPolicyOverride ov = turmaOverrideRepo.findByTurmaId(turmaId).orElse(null);
        return EffectiveAcademicPolicy.from(base, ov);
    }
}