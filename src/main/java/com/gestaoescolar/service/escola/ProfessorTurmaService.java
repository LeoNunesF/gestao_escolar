package com.gestaoescolar.service.escola;

import com.gestaoescolar.dto.TurmaResumoDTO;
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
import java.util.Optional;

@Service
@Transactional
public class ProfessorTurmaService {

    private final ProfessorTurmaRepository repo;
    private final TurmaRepository turmaRepository;
    private final EntityManager em;

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
        Optional<ProfessorTurma> exist = repo.findByProfessorIdAndTurmaId(professorId, turmaId);
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
            if (pt.getPapel() == ProfessorTurma.Papel.TITULAR && turma != null
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
            if (pt.getPapel() == ProfessorTurma.Papel.TITULAR && turma != null
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

    // NOVO: retorna DTOs prontos para a view
    public List<TurmaResumoDTO> listTurmasResumoByProfessor(Long professorId) {
        return repo.findTurmasResumoByProfessorId(professorId);
    }
}