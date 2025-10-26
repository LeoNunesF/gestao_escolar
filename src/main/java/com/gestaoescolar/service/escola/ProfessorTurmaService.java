package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.repository.ProfessorTurmaRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela associação Professor <-> Turma.
 * Usa EntityManager#getReference para evitar dependência direta de repositórios de Professor/Turma.
 */
@Service
@Transactional
public class ProfessorTurmaService {

    private final ProfessorTurmaRepository repo;
    private final EntityManager em;

    public ProfessorTurmaService(ProfessorTurmaRepository repo, EntityManager em) {
        this.repo = repo;
        this.em = em;
    }

    public ProfessorTurma assignProfessorToTurma(Long professorId, Long turmaId,
                                                 ProfessorTurma.Papel papel,
                                                 String disciplina,
                                                 LocalDate dataInicio,
                                                 LocalDate dataTermino) {
        // evita duplicação
        Optional<ProfessorTurma> exist = repo.findByProfessorIdAndTurmaId(professorId, turmaId);
        if (exist.isPresent()) {
            ProfessorTurma pt = exist.get();
            // atualiza metadados se necessário
            pt.setPapel(papel);
            pt.setDisciplina(disciplina);
            pt.setDataInicio(dataInicio);
            pt.setDataTermino(dataTermino);
            return repo.save(pt);
        }

        Professor professorRef = em.getReference(Professor.class, professorId);
        Turma turmaRef = em.getReference(Turma.class, turmaId);

        ProfessorTurma pt = new ProfessorTurma();
        pt.setProfessor(professorRef);
        pt.setTurma(turmaRef);
        pt.setPapel(papel);
        pt.setDisciplina(disciplina);
        pt.setDataInicio(dataInicio);
        pt.setDataTermino(dataTermino);

        return repo.save(pt);
    }

    public void removeAssignment(Long assignmentId) {
        repo.deleteById(assignmentId);
    }

    public void removeAssignment(Long professorId, Long turmaId) {
        Optional<ProfessorTurma> opt = repo.findByProfessorIdAndTurmaId(professorId, turmaId);
        opt.ifPresent(pt -> repo.delete(pt));
    }

    public List<ProfessorTurma> listByProfessor(Long professorId) {
        return repo.findByProfessorId(professorId);
    }

    public List<ProfessorTurma> listByTurma(Long turmaId) {
        return repo.findByTurmaId(turmaId);
    }
}