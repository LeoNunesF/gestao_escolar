package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.*;
import com.gestaoescolar.model.enums.PeriodType;
import com.gestaoescolar.repository.AcademicPeriodRepository;
import com.gestaoescolar.repository.AcademicPolicyRepository;
import com.gestaoescolar.repository.AnoLetivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AcademicPolicyService {

    private final AcademicPolicyRepository policyRepo;
    private final AcademicPeriodRepository periodRepo;
    private final AnoLetivoRepository anoLetivoRepo;

    public AcademicPolicyService(AcademicPolicyRepository policyRepo,
                                 AcademicPeriodRepository periodRepo,
                                 AnoLetivoRepository anoLetivoRepo) {
        this.policyRepo = policyRepo;
        this.periodRepo = periodRepo;
        this.anoLetivoRepo = anoLetivoRepo;
    }

    @Transactional
    public AcademicPolicy getOrCreatePolicy(Long anoLetivoId) {
        return policyRepo.findByAnoLetivoId(anoLetivoId).orElseGet(() -> {
            AnoLetivo ano = anoLetivoRepo.findById(anoLetivoId)
                    .orElseThrow(() -> new IllegalArgumentException("Ano letivo não encontrado."));
            AcademicPolicy p = new AcademicPolicy();
            p.setAnoLetivo(ano);
            p.setPeriodType(PeriodType.BIMESTRE);
            p.setTotalPeriods(4);
            p.setScaleMin(0.0);
            p.setScaleMax(10.0);
            p.setDecimalPrecision(1);
            p.setMinAverageForApproval(6.0);
            p.setMinAttendancePercent(75);
            p = policyRepo.save(p);
            regenerateDefaultPeriods(p.getId()); // cria 4 bimestres com nomes
            return p;
        });
    }

    // SUBSTITUA APENAS ESTE MÉTODO
    @Transactional
    public AcademicPolicy savePolicy(AcademicPolicy updated) {
        if (updated.getAnoLetivo() == null || updated.getAnoLetivo().getId() == null) {
            throw new IllegalArgumentException("Ano letivo é obrigatório.");
        }
        AcademicPolicy current = policyRepo.findByAnoLetivoId(updated.getAnoLetivo().getId())
                .orElseThrow(() -> new IllegalArgumentException("Política não encontrada para o ano letivo."));

        // Escala (numérica x conceitual)
        current.setEvaluationScaleType(updated.getEvaluationScaleType());
        current.setConceptLabels(updated.getConceptLabels());

        // Periodicidade
        current.setPeriodType(updated.getPeriodType());
        current.setTotalPeriods(updated.getTotalPeriods() != null ? updated.getTotalPeriods()
                : (updated.getPeriodType() == PeriodType.BIMESTRE ? 4 : 3));

        // Configs numéricas (aplicam-se quando escala NUMÉRICA)
        current.setScaleMin(updated.getScaleMin());
        current.setScaleMax(updated.getScaleMax());
        current.setDecimalPrecision(updated.getDecimalPrecision());
        current.setRoundingMode(updated.getRoundingMode());
        current.setMinAverageForApproval(updated.getMinAverageForApproval());
        current.setEvaluationWeighting(updated.getEvaluationWeighting());
        current.setTotalPointsPerPeriod(updated.getTotalPointsPerPeriod());

        // Frequência mínima (sempre aplicável)
        current.setMinAttendancePercent(updated.getMinAttendancePercent());

        // Regra de recuperação (por ora mantemos visível apenas para escala numérica)
        current.setRecoveryRule(updated.getRecoveryRule());

        return policyRepo.save(current);
    }

    @Transactional
    public void regenerateDefaultPeriods(Long policyId) {
        AcademicPolicy p = policyRepo.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Política acadêmica não encontrada."));
        // Remove existentes
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
}