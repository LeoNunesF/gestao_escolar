package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.*;
import com.gestaoescolar.model.enums.DisciplinaPadrao;
import com.gestaoescolar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CurriculumService {

    private final DisciplinaRepository disciplinaRepository;
    private final TurmaDisciplinaRepository turmaDisciplinaRepository;
    private final ProfessorTurmaDisciplinaRepository professorTurmaDisciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;

    public CurriculumService(DisciplinaRepository disciplinaRepository,
                             TurmaDisciplinaRepository turmaDisciplinaRepository,
                             ProfessorTurmaDisciplinaRepository professorTurmaDisciplinaRepository,
                             TurmaRepository turmaRepository,
                             ProfessorRepository professorRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.turmaDisciplinaRepository = turmaDisciplinaRepository;
        this.professorTurmaDisciplinaRepository = professorTurmaDisciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.professorRepository = professorRepository;
    }

    // ===== Disciplinas =====
    public List<Disciplina> listAllDisciplines() {
        return disciplinaRepository.findAll();
    }

    public Disciplina findDiscipline(Long id) {
        return disciplinaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Disciplina createOrUpdateDisciplina(Disciplina d) {
        if (d.getCodigo() == null || d.getCodigo().isBlank()) {
            throw new IllegalArgumentException("Código da disciplina é obrigatório");
        }
        if (d.getNome() == null || d.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome da disciplina é obrigatório");
        }
        return disciplinaRepository.save(d);
    }

    @Transactional
    public void deleteDisciplina(Long id) {
        disciplinaRepository.deleteById(id);
    }

    // Importa disciplinas padrão (cria apenas as que não existem pelo código)
    @Transactional
    public int importAllDefaultDisciplines() {
        int created = 0;
        for (DisciplinaPadrao dp : DisciplinaPadrao.values()) {
            boolean exists = disciplinaRepository.findByCodigo(dp.getCodigo()).isPresent();
            if (!exists) {
                Disciplina d = new Disciplina();
                d.setCodigo(dp.getCodigo());
                d.setNome(dp.getNome());
                d.setDescricao("Disciplina padrão (BR)");
                disciplinaRepository.save(d);
                created++;
            }
        }
        return created;
    }

    // ===== TurmaDisciplina =====
    public List<TurmaDisciplina> listByTurma(Long turmaId) {
        return turmaDisciplinaRepository.findByTurmaId(turmaId);
    }

    @Transactional
    public TurmaDisciplina addDisciplinaToTurma(Long turmaId, Long disciplinaId, Integer cargaHoraria) {
        Turma t = turmaRepository.findById(turmaId).orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));
        Disciplina d = disciplinaRepository.findById(disciplinaId).orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada"));

        TurmaDisciplina td = new TurmaDisciplina();
        td.setTurma(t);
        td.setDisciplina(d);
        td.setCargaHoraria(cargaHoraria);
        td.setAtiva(true);
        return turmaDisciplinaRepository.save(td);
    }

    @Transactional
    public void removeDisciplinaFromTurma(Long turmaDisciplinaId) {
        turmaDisciplinaRepository.deleteById(turmaDisciplinaId);
    }

    // ===== ProfessorTurmaDisciplina =====
    public List<ProfessorTurmaDisciplina> listProfessoresByTurmaDisciplina(Long turmaDisciplinaId) {
        return professorTurmaDisciplinaRepository.findByTurmaDisciplinaId(turmaDisciplinaId);
    }

    @Transactional
    public ProfessorTurmaDisciplina assignProfessorToTurmaDisciplina(Long turmaDisciplinaId, Long professorId, boolean titular) {
        TurmaDisciplina td = turmaDisciplinaRepository.findById(turmaDisciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Vínculo turma-disciplina não encontrado"));
        Professor p = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        ProfessorTurmaDisciplina ptd = new ProfessorTurmaDisciplina();
        ptd.setProfessor(p);
        ptd.setTurmaDisciplina(td);
        ptd.setTitular(titular);
        return professorTurmaDisciplinaRepository.save(ptd);
    }

    @Transactional
    public void unassignProfessor(Long professorTurmaDisciplinaId) {
        professorTurmaDisciplinaRepository.deleteById(professorTurmaDisciplinaId);
    }
}