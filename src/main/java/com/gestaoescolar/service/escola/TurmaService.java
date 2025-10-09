package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.NivelEscolar;
import com.gestaoescolar.model.enums.Serie;
import com.gestaoescolar.model.enums.Turno;
import com.gestaoescolar.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;

    public TurmaService(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    // CREATE - Criar nova turma
    @Transactional
    public Turma criarTurma(Turma novaTurma, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        validarDadosTurma(novaTurma);

        // Garantir que é uma turma nova
        novaTurma.setId(null);
        novaTurma.setAtiva(true);

        return turmaRepository.save(novaTurma);
    }

    // READ - Listar todas as turmas
    public List<Turma> listarTodasTurmas(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findAll();
    }

    // READ - Buscar turma por ID
    public Optional<Turma> buscarPorId(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findById(id);
    }

    // READ - Buscar turma por código
    public Optional<Turma> buscarPorCodigo(String codigo, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findByCodigo(codigo);
    }

    // READ - Listar turmas por ano letivo
    public List<Turma> listarPorAnoLetivo(AnoLetivo anoLetivo, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findByAnoLetivoOrderBySerieAndNome(anoLetivo);
    }

    // READ - Listar turmas ativas por ano letivo
    public List<Turma> listarAtivasPorAnoLetivo(AnoLetivo anoLetivo, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findByAnoLetivoAndAtivaTrue(anoLetivo);
    }

    // READ - Listar turmas por série
    public List<Turma> listarPorSerie(Serie serie, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findBySerie(serie);
    }

    // READ - Listar turmas por nível
    public List<Turma> listarPorNivel(NivelEscolar nivel, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findByNivel(nivel);
    }

    // READ - Listar turmas por turno
    public List<Turma> listarPorTurno(Turno turno, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findByTurno(turno);
    }

    // READ - Listar turmas com vagas
    public List<Turma> listarTurmasComVagas(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findTurmasComVagas();
    }

    // READ - Listar turmas com vagas por ano letivo
    public List<Turma> listarTurmasComVagasPorAnoLetivo(AnoLetivo anoLetivo, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findTurmasComVagasPorAnoLetivo(anoLetivo);
    }

    // READ - Listar turmas sem professor titular
    public List<Turma> listarTurmasSemProfessorTitular(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return turmaRepository.findTurmasSemProfessorTitular();
    }

    // UPDATE - Atualizar turma
    @Transactional
    public Turma atualizarTurma(Long id, Turma dadosAtualizados, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Turma turmaExistente = turmaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        validarAtualizacaoTurma(turmaExistente, dadosAtualizados);

        // Atualizar campos permitidos
        turmaExistente.setNomeTurma(dadosAtualizados.getNomeTurma());
        turmaExistente.setSerie(dadosAtualizados.getSerie());
        turmaExistente.setTurno(dadosAtualizados.getTurno());
        turmaExistente.setCapacidade(dadosAtualizados.getCapacidade());
        turmaExistente.setSala(dadosAtualizados.getSala());
        turmaExistente.setProfessorTitular(dadosAtualizados.getProfessorTitular());

        return turmaRepository.save(turmaExistente);
    }

    // UPDATE - Ativar/desativar turma
    @Transactional
    public void toggleStatusTurma(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        turma.setAtiva(!turma.isAtiva());
        turmaRepository.save(turma);
    }

    // UPDATE - Atribuir professor titular
    @Transactional
    public Turma atribuirProfessorTitular(Long turmaId, Long professorId, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        // Aqui você precisaria buscar o professor pelo ID
        // Professor professor = professorRepository.findById(professorId)...
        // turma.setProfessorTitular(professor);

        // Por enquanto, vamos apenas simular
        System.out.println("Atribuindo professor " + professorId + " à turma " + turmaId);

        return turmaRepository.save(turma);
    }

    // UPDATE - Remover professor titular
    @Transactional
    public Turma removerProfessorTitular(Long turmaId, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        turma.setProfessorTitular(null);
        return turmaRepository.save(turma);
    }

    // DELETE - Desativar turma (soft delete)
    @Transactional
    public void desativarTurma(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        // Validar se a turma pode ser desativada (ex: não tem alunos matriculados)
        validarDesativacaoTurma(turma);

        turma.setAtiva(false);
        turmaRepository.save(turma);
    }

    // DELETE - Reativar turma
    @Transactional
    public void reativarTurma(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        turma.setAtiva(true);
        turmaRepository.save(turma);
    }

    // VALIDAÇÕES
    private void validarPermissaoAdministrativa(Usuario usuario) {
        if (usuario == null || !usuario.isAdministrativo()) {
            throw new SecurityException("Acesso restrito à administração");
        }
    }

    private void validarDadosTurma(Turma turma) {
        if (turma.getNomeTurma() == null || turma.getNomeTurma().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da turma é obrigatório");
        }

        if (turma.getSerie() == null) {
            throw new IllegalArgumentException("Série é obrigatória");
        }

        if (turma.getTurno() == null) {
            throw new IllegalArgumentException("Turno é obrigatório");
        }

        if (turma.getAnoLetivo() == null) {
            throw new IllegalArgumentException("Ano letivo é obrigatório");
        }

        // Validar unicidade: não pode existir turma com mesmo nome/série/turno no mesmo ano
        if (turmaRepository.existsByAnoLetivoAndSerieAndNomeTurmaAndTurno(
                turma.getAnoLetivo(), turma.getSerie(), turma.getNomeTurma(), turma.getTurno())) {
            throw new IllegalArgumentException("Já existe uma turma com este nome, série e turno no ano letivo selecionado");
        }

        // Validar capacidade
        if (turma.getCapacidade() != null && turma.getCapacidade() <= 0) {
            throw new IllegalArgumentException("Capacidade deve ser maior que zero");
        }
    }

    private void validarAtualizacaoTurma(Turma turmaExistente, Turma dadosNovos) {
        // Validar se está tentando alterar o ano letivo (não permitido)
        if (!turmaExistente.getAnoLetivo().equals(dadosNovos.getAnoLetivo())) {
            throw new IllegalArgumentException("Não é possível alterar o ano letivo da turma");
        }

        // Validar unicidade se nome/série/turno foram alterados
        if (!turmaExistente.getNomeTurma().equals(dadosNovos.getNomeTurma()) ||
                !turmaExistente.getSerie().equals(dadosNovos.getSerie()) ||
                !turmaExistente.getTurno().equals(dadosNovos.getTurno())) {

            if (turmaRepository.existsByAnoLetivoAndSerieAndNomeTurmaAndTurno(
                    turmaExistente.getAnoLetivo(), dadosNovos.getSerie(),
                    dadosNovos.getNomeTurma(), dadosNovos.getTurno())) {
                throw new IllegalArgumentException("Já existe uma turma com este nome, série e turno no ano letivo");
            }
        }
    }

    private void validarDesativacaoTurma(Turma turma) {
        // Futuro: validar se a turma tem alunos matriculados
        // if (turma.getAlunosMatriculados() > 0) {
        //     throw new IllegalArgumentException("Não é possível desativar turma com alunos matriculados");
        // }

        // Por enquanto, apenas log
        System.out.println("Validando desativação da turma: " + turma.getDescricaoCompleta());
    }

    // MÉTODOS AUXILIARES E ESTATÍSTICAS
    public long contarTurmasAtivas() {
        return turmaRepository.findByAtivaTrue().size();
    }

    public long contarTurmasPorAnoLetivo(AnoLetivo anoLetivo) {
        return turmaRepository.countByAnoLetivo(anoLetivo);
    }

    public long contarTurmasAtivasPorAnoLetivo(AnoLetivo anoLetivo) {
        return turmaRepository.countByAnoLetivoAndAtivaTrue(anoLetivo);
    }

    public boolean existeTurmaComCodigo(String codigo) {
        return turmaRepository.findByCodigo(codigo).isPresent();
    }

    public List<Turma> buscarTurmasPorNome(String nome, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        // Implementação futura: busca por parte do nome
        return turmaRepository.findAll().stream()
                .filter(t -> t.getNomeTurma().toLowerCase().contains(nome.toLowerCase()))
                .toList();
    }
}