package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.*;
import com.gestaoescolar.model.enums.DisciplinaPadrao;
import com.gestaoescolar.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
        if (d == null) throw new IllegalArgumentException("Dados da disciplina não informados.");
        String codigo = d.getCodigo() != null ? d.getCodigo().trim() : "";
        String nome = d.getNome() != null ? d.getNome().trim() : "";

        if (codigo.isBlank()) throw new IllegalArgumentException("Código da disciplina é obrigatório.");
        if (nome.isBlank()) throw new IllegalArgumentException("Nome da disciplina é obrigatório.");

        // Verificação case-insensitive
        var existenteCI = disciplinaRepository.findByCodigoIgnoreCase(codigo);
        if (existenteCI.isPresent()) {
            Disciplina existente = existenteCI.get();
            // Se for criação (id null) ou se for edição trocando para um código que já pertence a outro id
            if (d.getId() == null || !Objects.equals(existente.getId(), d.getId())) {
                throw new IllegalArgumentException("Já existe uma disciplina com este código (ignora maiúsculas/minúsculas).");
            }
        }

        d.setCodigo(codigo); // salva código já aparado (sem espaços antes/depois)
        d.setNome(nome);

        try {
            return disciplinaRepository.save(d);
        } catch (DataIntegrityViolationException ex) {
            // Fallback amigável para violação da constraint unique do banco
            throw new IllegalArgumentException("Código de disciplina já utilizado. Escolha um código diferente.");
        }
    }

    @Transactional
    public void deleteDisciplina(Long id) {
        disciplinaRepository.deleteById(id);
    }

    // Importa disciplinas padrão (cria apenas as que não existem pelo código - case-insensitive)
    @Transactional
    public int importAllDefaultDisciplines() {
        int created = 0;
        for (DisciplinaPadrao dp : DisciplinaPadrao.values()) {
            boolean exists = disciplinaRepository.existsByCodigoIgnoreCase(dp.getCodigo());
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
        Turma t = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada."));
        Disciplina d = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada."));

        // Impede duplicidade na mesma turma (cheque simples)
        boolean jaExiste = turmaDisciplinaRepository.existsByTurmaIdAndDisciplinaId(turmaId, disciplinaId);
        if (jaExiste) {
            String nomeDisc = d.getNome() != null ? d.getNome() : "Disciplina";
            throw new IllegalArgumentException("Esta turma já possui a disciplina \"" + nomeDisc + "\".");
        }

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
                .orElseThrow(() -> new IllegalArgumentException("Vínculo turma-disciplina não encontrado."));
        Professor p = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado."));

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