package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.PerfilUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "login"),
        @UniqueConstraint(columnNames = "email")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Login é obrigatório")
    @Size(min = 3, max = 50, message = "Login deve ter entre 3 e 50 caracteres")
    @Column(unique = true, nullable = false)
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Perfil é obrigatório")
    private PerfilUsuario perfil;

    private boolean ativo = true;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime ultimoAcesso;

    // Construtores
    public Usuario() {}

    public Usuario(String login, String senha, String email, String nomeCompleto, PerfilUsuario perfil) {
        this.login = login;
        this.senha = senha;
        this.email = email;
        this.nomeCompleto = nomeCompleto;
        this.perfil = perfil;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(LocalDateTime ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }

    // Métodos auxiliares
    public boolean isDiretor() {
        return perfil != null && perfil.isDiretor();
    }

    public boolean isAdministrativo() {
        return perfil != null && perfil.isAdministrativo();
    }

    public void atualizarUltimoAcesso() {
        this.ultimoAcesso = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return nomeCompleto + " (" + login + ")";
    }
}