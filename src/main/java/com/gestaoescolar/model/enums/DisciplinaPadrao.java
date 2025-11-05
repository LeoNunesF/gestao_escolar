package com.gestaoescolar.model.enums;

public enum DisciplinaPadrao {
    PORTUGUES("PORT", "Língua Portuguesa"),
    LITERATURA("LIT", "Literatura"),
    REDACAO("RED", "Redação"),
    MATEMATICA("MAT", "Matemática"),
    CIENCIAS("CIE", "Ciências"),
    HISTORIA("HIS", "História"),
    GEOGRAFIA("GEO", "Geografia"),
    ARTE("ART", "Arte"),
    EDUCACAO_FISICA("EDF", "Educação Física"),
    ENSINO_RELIGIOSO("REL", "Ensino Religioso"),
    INGLES("ING", "Inglês"),
    ESPANHOL("ESP", "Espanhol"),
    FISICA("FIS", "Física"),
    QUIMICA("QUI", "Química"),
    BIOLOGIA("BIO", "Biologia"),
    SOCIOLOGIA("SOC", "Sociologia"),
    FILOSOFIA("FIL", "Filosofia"),
    TECNOLOGIAS("TEC", "Tecnologias"),
    PROJETO_DE_VIDA("PV", "Projeto de Vida");

    private final String codigo;
    private final String nome;

    DisciplinaPadrao(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }
}