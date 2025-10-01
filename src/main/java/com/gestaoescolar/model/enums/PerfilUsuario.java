package com.gestaoescolar.model.enums;

public enum PerfilUsuario {
    DIRETOR("Diretor", "Acesso completo ao sistema"),
    SECRETARIA("Secretaria", "Gestão administrativa e cadastros"),
    PROFESSOR("Professor", "Acesso às turmas e diário de classe"),
    RESPONSAVEL("Responsável", "Acesso ao portal de pais");

    private final String nome;
    private final String descricao;

    PerfilUsuario(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    // Método para verificar se tem permissão de administração
    public boolean isAdministrativo() {
        return this == DIRETOR || this == SECRETARIA;
    }

    // Método para verificar se é diretor
    public boolean isDiretor() {
        return this == DIRETOR;
    }

    public static PerfilUsuario fromNome(String nome) {
        for (PerfilUsuario perfil : values()) {
            if (perfil.nome.equalsIgnoreCase(nome)) {
                return perfil;
            }
        }
        throw new IllegalArgumentException("Perfil não encontrado: " + nome);
    }
}