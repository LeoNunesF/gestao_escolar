package com.gestaoescolar.service.auth;

import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.PerfilUsuario;
import com.gestaoescolar.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // CREATE - Criar novo usuário (apenas diretor)
    @Transactional
    public Usuario criarUsuario(Usuario novoUsuario, Usuario diretorLogado) {
        validarPermissaoDiretor(diretorLogado);
        validarDadosNovoUsuario(novoUsuario);

        // Garantir que é um usuário novo
        novoUsuario.setId(null);
        novoUsuario.setAtivo(true);

        return usuarioRepository.save(novoUsuario);
    }

    // READ - Listar todos os usuários (apenas diretor/secretaria)
    public List<Usuario> listarTodosUsuarios(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return usuarioRepository.findAll();
    }

    // READ - Buscar usuário por ID
    public Optional<Usuario> buscarPorId(Long id, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return usuarioRepository.findById(id);
    }

    // READ - Buscar usuários por perfil
    public List<Usuario> buscarPorPerfil(PerfilUsuario perfil, Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return usuarioRepository.findByPerfil(perfil);
    }

    // READ - Listar usuários ativos
    public List<Usuario> listarUsuariosAtivos(Usuario usuarioLogado) {
        validarPermissaoAdministrativa(usuarioLogado);
        return usuarioRepository.findByAtivoTrue();
    }

    // UPDATE - Atualizar usuário completo (apenas diretor)
    @Transactional
    public Usuario atualizarUsuario(Long id, Usuario dadosAtualizados, Usuario diretorLogado) {
        validarPermissaoDiretor(diretorLogado);

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        validarAtualizacaoUsuario(usuarioExistente, dadosAtualizados, diretorLogado);

        // Atualizar campos permitidos
        usuarioExistente.setNomeCompleto(dadosAtualizados.getNomeCompleto());
        usuarioExistente.setEmail(dadosAtualizados.getEmail());
        usuarioExistente.setPerfil(dadosAtualizados.getPerfil());

        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isEmpty()) {
            usuarioExistente.setSenha(dadosAtualizados.getSenha());
        }

        return usuarioRepository.save(usuarioExistente);
    }

    // UPDATE - Alterar perfil específico (apenas diretor)
    @Transactional
    public Usuario alterarPerfil(Long usuarioId, PerfilUsuario novoPerfil, Usuario diretorLogado) {
        validarPermissaoDiretor(diretorLogado);
        validarNaoAutoEdicao(usuarioId, diretorLogado, "alterar o perfil");

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        validarUltimoDiretor(usuario, novoPerfil);

        usuario.setPerfil(novoPerfil);
        return usuarioRepository.save(usuario);
    }

    // DELETE - Exclusão lógica (desativar usuário)
    @Transactional
    public void desativarUsuario(Long id, Usuario diretorLogado) {
        validarPermissaoDiretor(diretorLogado);
        validarNaoAutoEdicao(id, diretorLogado, "desativar");

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        validarUltimoDiretor(usuario, usuario.getPerfil());

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    // DELETE - Reativar usuário
    @Transactional
    public void reativarUsuario(Long id, Usuario diretorLogado) {
        validarPermissaoDiretor(diretorLogado);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    // DELETE - Exclusão física (apenas para testes/limpeza)
    @Transactional
    public void excluirUsuarioFisicamente(Long id, Usuario diretorLogado) {
        validarPermissaoDiretor(diretorLogado);
        validarNaoAutoEdicao(id, diretorLogado, "excluir");

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        validarUltimoDiretor(usuario, usuario.getPerfil());

        usuarioRepository.delete(usuario);
    }

    // VALIDAÇÕES DE SEGURANÇA
    private void validarPermissaoDiretor(Usuario usuario) {
        if (usuario == null || !usuario.isDiretor()) {
            throw new SecurityException("Apenas diretores podem realizar esta operação");
        }
    }

    private void validarPermissaoAdministrativa(Usuario usuario) {
        if (usuario == null || !usuario.isAdministrativo()) {
            throw new SecurityException("Acesso restrito à administração");
        }
    }

    private void validarDadosNovoUsuario(Usuario usuario) {
        if (usuario.getLogin() == null || usuario.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("Login é obrigatório");
        }

        if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new IllegalArgumentException("Já existe um usuário com este login");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Já existe um usuário com este email");
        }
    }

    private void validarAtualizacaoUsuario(Usuario usuarioExistente, Usuario dadosNovos, Usuario diretorLogado) {
        validarNaoAutoEdicao(usuarioExistente.getId(), diretorLogado, "editar");

        // Validar unicidade do login (se foi alterado)
        if (!usuarioExistente.getLogin().equals(dadosNovos.getLogin()) &&
                usuarioRepository.existsByLogin(dadosNovos.getLogin())) {
            throw new IllegalArgumentException("Já existe um usuário com este login");
        }

        // Validar unicidade do email (se foi alterado)
        if (!usuarioExistente.getEmail().equals(dadosNovos.getEmail()) &&
                usuarioRepository.existsByEmail(dadosNovos.getEmail())) {
            throw new IllegalArgumentException("Já existe um usuário com este email");
        }
    }

    private void validarNaoAutoEdicao(Long usuarioId, Usuario usuarioLogado, String operacao) {
        if (usuarioId.equals(usuarioLogado.getId())) {
            throw new SecurityException("Você não pode " + operacao + " seu próprio usuário");
        }
    }

    private void validarUltimoDiretor(Usuario usuario, PerfilUsuario novoPerfil) {
        // Se está tentando alterar o perfil de um diretor, verificar se não é o último
        if (usuario.isDiretor() && !novoPerfil.isDiretor()) {
            long countDiretoresAtivos = usuarioRepository.findByPerfil(PerfilUsuario.DIRETOR)
                    .stream()
                    .filter(Usuario::isAtivo)
                    .count();

            if (countDiretoresAtivos <= 1) {
                throw new SecurityException("Não é possível alterar o perfil do último diretor ativo");
            }
        }
    }

    // MÉTODOS AUXILIARES
    public long contarUsuariosAtivos() {
        return usuarioRepository.countUsuariosAtivos();
    }

    public boolean existeUsuarioComLogin(String login) {
        return usuarioRepository.existsByLogin(login);
    }

    public boolean existeUsuarioComEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}