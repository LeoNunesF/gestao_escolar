package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.repository.ProfessorTurmaRepository;
import com.gestaoescolar.repository.ProfessorRepository;
import com.gestaoescolar.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProfessorTurmaService {

    private final ProfessorTurmaRepository professorTurmaRepository;
    private final ProfessorRepository professorRepository;
    private final TurmaRepository turmaRepository;

    public ProfessorTurmaService(ProfessorTurmaRepository professorTurmaRepository,
                                 ProfessorRepository professorRepository,
                                 TurmaRepository turmaRepository) {
        this.professorTurmaRepository = professorTurmaRepository;
        this.professorRepository = professorRepository;
        this.turmaRepository = turmaRepository;
    }

    // CREATE - Atribuir professor a turma
    @Transactional
    public ProfessorTurma assignProfessorToTurma(Long professorId, Long turmaId, 
                                                   String papel, String disciplina, 
                                                   Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        // Verificar se professor existe
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        // Verificar se turma existe
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        // Verificar se já existe atribuição
        if (professorTurmaRepository.existsByProfessorIdAndTurmaId(professorId, turmaId)) {
            throw new IllegalArgumentException("Professor já está atribuído a esta turma");
        }

        // Criar nova atribuição
        ProfessorTurma professorTurma = new ProfessorTurma(professor, turma, papel, disciplina);
        professorTurma.setDataInicio(LocalDate.now());

        return professorTurmaRepository.save(professorTurma);
    }

    // CREATE - Atribuir professor a turma (versão simplificada)
    @Transactional
    public ProfessorTurma assignProfessorToTurma(Long professorId, Long turmaId, Usuario usuarioLogado) {
        return assignProfessorToTurma(professorId, turmaId, null, null, usuarioLogado);
    }

    // READ - Listar todas as turmas de um professor
    public List<ProfessorTurma> listByProfessor(Long professorId) {
        return professorTurmaRepository.findByProfessorId(professorId);
    }

    // READ - Listar todos os professores de uma turma
    public List<ProfessorTurma> listByTurma(Long turmaId) {
        return professorTurmaRepository.findByTurmaId(turmaId);
    }

    // READ - Listar atribuições ativas de um professor
    public List<ProfessorTurma> listAtivasByProfessor(Long professorId) {
        return professorTurmaRepository.findAtivasByProfessorId(professorId);
    }

    // READ - Listar atribuições ativas de uma turma
    public List<ProfessorTurma> listAtivasByTurma(Long turmaId) {
        return professorTurmaRepository.findAtivasByTurmaId(turmaId);
    }

    // READ - Buscar por ID
    public Optional<ProfessorTurma> findById(Long id) {
        return professorTurmaRepository.findById(id);
    }

    // READ - Buscar atribuição específica
    public Optional<ProfessorTurma> findByProfessorAndTurma(Long professorId, Long turmaId) {
        return professorTurmaRepository.findByProfessorIdAndTurmaId(professorId, turmaId);
    }

    // UPDATE - Atualizar atribuição
    @Transactional
    public ProfessorTurma updateAssignment(Long id, String papel, String disciplina, 
                                           LocalDate dataInicio, LocalDate dataTermino,
                                           Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        ProfessorTurma professorTurma = professorTurmaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atribuição não encontrada"));

        if (papel != null) {
            professorTurma.setPapel(papel);
        }
        if (disciplina != null) {
            professorTurma.setDisciplina(disciplina);
        }
        if (dataInicio != null) {
            professorTurma.setDataInicio(dataInicio);
        }
        if (dataTermino != null) {
            professorTurma.setDataTermino(dataTermino);
        }

        return professorTurmaRepository.save(professorTurma);
    }

    // DELETE - Remover atribuição (encerrar)
    @Transactional
    public void removeAssignment(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        ProfessorTurma professorTurma = professorTurmaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atribuição não encontrada"));

        // Soft delete: definir data de término
        professorTurma.setDataTermino(LocalDate.now());
        professorTurmaRepository.save(professorTurma);
    }

    // DELETE - Remover atribuição permanentemente
    @Transactional
    public void deleteAssignment(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        if (!professorTurmaRepository.existsById(id)) {
            throw new IllegalArgumentException("Atribuição não encontrada");
        }

        professorTurmaRepository.deleteById(id);
    }

    // VALIDAÇÕES
    private void validarPermissaoAdministrativa(Usuario usuario) {
        if (usuario == null || !usuario.isAdministrativo()) {
            throw new SecurityException("Acesso restrito à administração");
        }
    }

    // MÉTODOS AUXILIARES
    public long countByProfessor(Long professorId) {
        return professorTurmaRepository.findByProfessorId(professorId).size();
    }

    public long countByTurma(Long turmaId) {
        return professorTurmaRepository.findByTurmaId(turmaId).size();
    }

    public boolean isProfessorAssignedToTurma(Long professorId, Long turmaId) {
        return professorTurmaRepository.existsByProfessorIdAndTurmaId(professorId, turmaId);
    }

    public List<Turma> getTurmasByProfessor(Long professorId) {
        return professorTurmaRepository.findByProfessorId(professorId)
                .stream()
                .map(ProfessorTurma::getTurma)
                .collect(Collectors.toList());
    }

    public List<Professor> getProfessoresByTurma(Long turmaId) {
        return professorTurmaRepository.findByTurmaId(turmaId)
                .stream()
                .map(ProfessorTurma::getProfessor)
                .collect(Collectors.toList());
    }
}
