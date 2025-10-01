package com.gestaoescolar.service.auth;

import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final Map<String, Usuario> sessoesAtivas = new HashMap<>();
    private Usuario usuarioLogado;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        criarUsuarioPadrao(); // Para testes
    }

    public boolean login(String login, String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(login);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // SIMULAÇÃO: Em produção, usar BCrypt
            if (usuario.isAtivo() && senha.equals(usuario.getSenha())) {
                this.usuarioLogado = usuario;
                usuario.atualizarUltimoAcesso();
                usuarioRepository.save(usuario);
                return true;
            }
        }
        return false;
    }

    public void logout() {
        this.usuarioLogado = null;
    }

    public boolean isUsuarioLogado() {
        return usuarioLogado != null;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    private void criarUsuarioPadrao() {
        // Criar usuário diretor padrão se não existir
        if (usuarioRepository.count() == 0) {
            Usuario diretor = new Usuario();
            diretor.setLogin("admin");
            diretor.setSenha("123456"); // Em produção, usar senha hash
            diretor.setEmail("diretor@escola.com");
            diretor.setNomeCompleto("Diretor Administrativo");
            diretor.setPerfil(com.gestaoescolar.model.enums.PerfilUsuario.DIRETOR);

            usuarioRepository.save(diretor);
            System.out.println("✅ Usuário diretor padrão criado: admin / 123456");
        }
    }
}