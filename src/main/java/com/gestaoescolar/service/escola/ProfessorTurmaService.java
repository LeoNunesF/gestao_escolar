package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.repository.ProfessorTurmaRepository;
import com.gestaoescolar.repository.TurmaRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Serviço responsável pela associação Professor <-> Turma.
 * Regras de negócio adicionadas:
 * - 1 titular por turma (considerando período).
 * - Impede atribuir professor inativo/demitido.
 * - Valida período (início <= término).
 * - Evita sobreposição de períodos para mesmo professor/turma/mesma disciplina.
 */
@Service
@Transactional
public class ProfessorTurmaService {

    private final ProfessorTurmaRepository repo;
    private final TurmaRepository turmaRepository;
    private final EntityManager em;
    // Alias em inglês para padronização de chamadas na UI
    public java.util.List<com.gestaoescolar.dto.VinculoProfessorTurmaDTO> listAssignmentSummariesByProfessor(Long professorId) {
        return repo.findVinculosResumoByProfessorId(professorId);
    }

    public ProfessorTurmaService(ProfessorTurmaRepository repo, TurmaRepository turmaRepository, EntityManager em) {
        this.repo = repo;
        this.turmaRepository = turmaRepository;
        this.em = em;
    }

    public ProfessorTurma assignProfessorToTurma(Long professorId, Long turmaId,
                                                 ProfessorTurma.Papel papel,
                                                 String disciplina,
                                                 LocalDate dataInicio,
                                                 LocalDate dataTermino) {
        // Se já existe vínculo entre o mesmo professor e turma, faremos update
        Optional<ProfessorTurma> exist = repo.findByProfessorIdAndTurmaId(professorId, turmaId);

        Long currentAssignmentId = exist.map(ProfessorTurma::getId).orElse(null);
        validarAtribuicao(professorId, turmaId, papel, disciplina, dataInicio, dataTermino, currentAssignmentId);

        Professor professorRef = em.getReference(Professor.class, professorId);
        Turma turmaRef = em.getReference(Turma.class, turmaId);

        if (exist.isPresent()) {
            ProfessorTurma pt = exist.get();
            pt.setPapel(papel);
            pt.setDisciplina(disciplina);
            pt.setDataInicio(dataInicio);
            pt.setDataTermino(dataTermino);
            pt = repo.save(pt);

            if (papel == ProfessorTurma.Papel.TITULAR) {
                turmaRef.setProfessorTitular(professorRef);
                turmaRepository.save(turmaRef);
            }
            return pt;
        }

        ProfessorTurma pt = new ProfessorTurma();
        pt.setProfessor(professorRef);
        pt.setTurma(turmaRef);
        pt.setPapel(papel);
        pt.setDisciplina(disciplina);
        pt.setDataInicio(dataInicio);
        pt.setDataTermino(dataTermino);
        pt = repo.save(pt);

        if (papel == ProfessorTurma.Papel.TITULAR) {
            turmaRef.setProfessorTitular(professorRef);
            turmaRepository.save(turmaRef);
        }

        return pt;
    }

    public void removeAssignment(Long assignmentId) {
        repo.findById(assignmentId).ifPresent(pt -> {
            Turma turma = pt.getTurma();
            if (pt.getPapel() == ProfessorTurma.Papel.TITULAR
                    && turma != null
                    && turma.getProfessorTitular() != null
                    && turma.getProfessorTitular().equals(pt.getProfessor())) {
                turma.setProfessorTitular(null);
                turmaRepository.save(turma);
            }
            repo.deleteById(assignmentId);
        });
    }

    public void removeAssignment(Long professorId, Long turmaId) {
        Optional<ProfessorTurma> opt = repo.findByProfessorIdAndTurmaId(professorId, turmaId);
        if (opt.isPresent()) {
            ProfessorTurma pt = opt.get();
            Turma turma = pt.getTurma();
            if (pt.getPapel() == ProfessorTurma.Papel.TITULAR
                    && turma != null
                    && turma.getProfessorTitular() != null
                    && turma.getProfessorTitular().getId().equals(professorId)) {
                turma.setProfessorTitular(null);
                turmaRepository.save(turma);
            }
            repo.delete(pt);
        }
    }

    public List<ProfessorTurma> listByProfessor(Long professorId) {
        return repo.findByProfessorId(professorId);
    }

    public List<ProfessorTurma> listByTurma(Long turmaId) {
        return repo.findByTurmaId(turmaId);
    }

    // ===================== Validações de negócio =====================

    private void validarAtribuicao(Long professorId,
                                   Long turmaId,
                                   ProfessorTurma.Papel papel,
                                   String disciplina,
                                   LocalDate dataInicio,
                                   LocalDate dataTermino,
                                   Long currentAssignmentId) {
        // 1) Datas válidas
        if (dataInicio != null && dataTermino != null && dataInicio.isAfter(dataTermino)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de término.");
        }

        // 2) Professor válido e ativo
        Professor prof = em.find(Professor.class, professorId);
        if (prof == null) {
            throw new IllegalArgumentException("Professor não encontrado.");
        }
        if (!prof.isAtivo() || prof.getDataDemissao() != null) {
            throw new IllegalArgumentException("Não é possível atribuir um professor inativo ou demitido.");
        }

        // 3) Regra do Titular único na turma (considerando período)
        if (papel == ProfessorTurma.Papel.TITULAR) {
            List<ProfessorTurma> daTurma = repo.findByTurmaId(turmaId);
            for (ProfessorTurma pt : daTurma) {
                if (pt.getId() != null && currentAssignmentId != null && pt.getId().equals(currentAssignmentId)) {
                    continue; // é o próprio registro em atualização
                }
                if (pt.getPapel() == ProfessorTurma.Papel.TITULAR) {
                    // Verifica sobreposição de período
                    if (periodOverlap(dataInicio, dataTermino, pt.getDataInicio(), pt.getDataTermino())) {
                        throw new IllegalArgumentException("Já existe um professor titular para esta turma no período informado.");
                    }
                }
            }
        }

        // 4) Evitar sobreposição para mesma disciplina (quando informada)
        String discNorm = normalizeDisciplina(disciplina);
        if (!discNorm.isBlank()) {
            List<ProfessorTurma> doProfessor = repo.findByProfessorId(professorId);
            for (ProfessorTurma pt : doProfessor) {
                if (pt.getId() != null && currentAssignmentId != null && pt.getId().equals(currentAssignmentId)) {
                    continue; // é o próprio em atualização
                }
                if (pt.getTurma() != null
                        && pt.getTurma().getId() != null
                        && pt.getTurma().getId().equals(turmaId)) {
                    String discExist = normalizeDisciplina(pt.getDisciplina());
                    if (!discExist.isBlank() && discExist.equals(discNorm)) {
                        if (periodOverlap(dataInicio, dataTermino, pt.getDataInicio(), pt.getDataTermino())) {
                            throw new IllegalArgumentException("Já existe um vínculo para esta disciplina nesta turma no período informado.");
                        }
                    }
                }
            }
        }
    }

    private boolean periodOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        LocalDate as = aStart != null ? aStart : LocalDate.MIN;
        LocalDate ae = aEnd != null ? aEnd : LocalDate.MAX;
        LocalDate bs = bStart != null ? bStart : LocalDate.MIN;
        LocalDate be = bEnd != null ? bEnd : LocalDate.MAX;
        return !ae.isBefore(bs) && !be.isBefore(as);
    }

    private String normalizeDisciplina(String d) {
        if (d == null) return "";
        return d.trim().toLowerCase(Locale.ROOT);
    }
}