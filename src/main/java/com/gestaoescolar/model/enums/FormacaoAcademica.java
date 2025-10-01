package com.gestaoescolar.model.enums;

public enum FormacaoAcademica {
    ENSINO_MEDIO("Ensino Médio"),
    GRADUACAO_INCOMPLETA("Graduação Incompleta"),
    GRADUACAO_COMPLETA("Graduação Completa"),
    ESPECIALIZACAO("Especialização"),
    MESTRADO("Mestrado"),
    DOUTORADO("Doutorado");

    private final String descricao;

    FormacaoAcademica(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}