package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.FormacaoAcademica;
import com.gestaoescolar.model.enums.Genero;
import com.gestaoescolar.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    // CREATE - Criar novo professor
    @Transactional
    public Professor criarProfessor(Professor novoProfessor, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        validarDadosProfessor(novoProfessor);

        // Garantir que é um professor novo
        novoProfessor.setId(null);
        novoProfessor.setAtivo(true);

        if (novoProfessor.getDataAdmissao() == null) {
            novoProfessor.setDataAdmissao(LocalDate.now());
        }

        return professorRepository.save(novoProfessor);
    }

    // READ - Listar todos os professores
    public List<Professor> listarTodosProfessores(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findAllByOrderByNomeCompleto();
    }

    // READ - Buscar professor por ID
    public Optional<Professor> buscarPorId(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findById(id);
    }

    // READ - Buscar professor por CPF
    public Optional<Professor> buscarPorCpf(String cpf, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findByCpf(cpf);
    }

    // READ - Listar professores ativos
    public List<Professor> listarProfessoresAtivos(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findByAtivoTrue();
    }

    // READ - Listar professores inativos
    public List<Professor> listarProfessoresInativos(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findByAtivoFalse();
    }

    // READ - Listar professores por formação
    public List<Professor> listarPorFormacao(FormacaoAcademica formacao, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findByFormacao(formacao);
    }

    // READ - Listar professores com especialização
    public List<Professor> listarComEspecializacao(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findProfessoresComEspecializacao();
    }

    // READ - Listar professores com formação superior
    public List<Professor> listarComFormacaoSuperior(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        List<FormacaoAcademica> formacoesSuperiores = Arrays.asList(
                FormacaoAcademica.GRADUACAO_COMPLETA,
                FormacaoAcademica.ESPECIALIZACAO,
                FormacaoAcademica.MESTRADO,
                FormacaoAcademica.DOUTORADO
        );
        return professorRepository.findProfessoresComFormacaoSuperior(formacoesSuperiores);
    }

    // READ - Buscar professores por nome
    public List<Professor> buscarPorNome(String nome, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findByNomeContainingIgnoreCase(nome);
    }

    // READ - Listar professores por cidade
    public List<Professor> listarPorCidade(String cidade, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findByCidade(cidade);
    }

    // UPDATE - Atualizar professor
    @Transactional
    public Professor atualizarProfessor(Long id, Professor dadosAtualizados, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Professor professorExistente = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        validarAtualizacaoProfessor(professorExistente, dadosAtualizados);

        // Atualizar campos permitidos
        professorExistente.setNomeCompleto(dadosAtualizados.getNomeCompleto());
        professorExistente.setRg(dadosAtualizados.getRg());
        professorExistente.setEmail(dadosAtualizados.getEmail());
        professorExistente.setTelefone(dadosAtualizados.getTelefone());
        professorExistente.setDataNascimento(dadosAtualizados.getDataNascimento());
        professorExistente.setGenero(dadosAtualizados.getGenero());
        professorExistente.setEndereco(dadosAtualizados.getEndereco());
        professorExistente.setFormacao(dadosAtualizados.getFormacao());
        professorExistente.setEspecializacao(dadosAtualizados.getEspecializacao());
        professorExistente.setObservacoes(dadosAtualizados.getObservacoes());

        return professorRepository.save(professorExistente);
    }

    // UPDATE - Ativar/desativar professor
    @Transactional
    public void toggleStatusProfessor(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        professor.setAtivo(!professor.isAtivo());
        professorRepository.save(professor);
    }

    // UPDATE - Demitir professor
    @Transactional
    public Professor demitirProfessor(Long id, LocalDate dataDemissao, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        if (professor.getDataDemissao() != null) {
            throw new IllegalArgumentException("Professor já está demitido");
        }

        professor.setDataDemissao(dataDemissao != null ? dataDemissao : LocalDate.now());
        professor.setAtivo(false);

        return professorRepository.save(professor);
    }

    // UPDATE - Readmitir professor
    @Transactional
    public Professor readmitirProfessor(Long id, LocalDate dataAdmissao, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        professor.setDataDemissao(null);
        professor.setDataAdmissao(dataAdmissao != null ? dataAdmissao : LocalDate.now());
        professor.setAtivo(true);

        return professorRepository.save(professor);
    }

    // DELETE - Desativar professor (soft delete)
    @Transactional
    public void desativarProfessor(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        professor.setAtivo(false);
        professorRepository.save(professor);
    }

    // DELETE - Reativar professor
    @Transactional
    public void reativarProfessor(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);

        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        professor.setAtivo(true);
        professorRepository.save(professor);
    }

    // VALIDAÇÕES
    private void validarPermissaoAdministrativa(Usuario usuario) {
        if (usuario == null || !usuario.isAdministrativo()) {
            throw new SecurityException("Acesso restrito à administração");
        }
    }

    private void validarDadosProfessor(Professor professor) {
        if (professor.getNomeCompleto() == null || professor.getNomeCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome completo é obrigatório");
        }

        if (professor.getCpf() == null || professor.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }

        if (professor.getEmail() == null || professor.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        if (professor.getTelefone() == null || professor.getTelefone().trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }

        if (professor.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }

        if (professor.getGenero() == null) {
            throw new IllegalArgumentException("Gênero é obrigatório");
        }

        if (professor.getFormacao() == null) {
            throw new IllegalArgumentException("Formação acadêmica é obrigatória");
        }

        // Validar CPF único
        if (professorRepository.existsByCpf(professor.getCpf())) {
            throw new IllegalArgumentException("Já existe um professor com este CPF");
        }

        // Validar email único
        if (professorRepository.existsByEmail(professor.getEmail())) {
            throw new IllegalArgumentException("Já existe um professor com este email");
        }

        // Validar idade mínima (18 anos)
        if (professor.getIdade() != null && professor.getIdade() < 18) {
            throw new IllegalArgumentException("Professor deve ter pelo menos 18 anos");
        }
    }

    private void validarAtualizacaoProfessor(Professor professorExistente, Professor dadosNovos) {
        // Validar se está tentando alterar CPF (não permitido)
        if (!professorExistente.getCpf().equals(dadosNovos.getCpf())) {
            throw new IllegalArgumentException("Não é possível alterar o CPF do professor");
        }

        // Validar email único se foi alterado
        if (!professorExistente.getEmail().equals(dadosNovos.getEmail()) &&
                professorRepository.existsByEmail(dadosNovos.getEmail())) {
            throw new IllegalArgumentException("Já existe um professor com este email");
        }
    }

    // MÉTODOS AUXILIARES E ESTATÍSTICAS
    public long contarProfessoresAtivos() {
        return professorRepository.countByAtivoTrue();
    }

    public long contarProfessoresPorGenero(Genero genero) {
        return professorRepository.countByGenero(genero);
    }

    public long contarProfessoresPorFormacao(FormacaoAcademica formacao) {
        return professorRepository.countByFormacao(formacao);
    }

    public boolean existeProfessorComCpf(String cpf) {
        return professorRepository.existsByCpf(cpf);
    }

    public boolean existeProfessorComEmail(String email) {
        return professorRepository.existsByEmail(email);
    }

    public List<Professor> listarProfessoresPorEstado(String estado, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return professorRepository.findByEstado(estado);
    }
}