package com.gestaoescolar.model.enums;

public enum Genero {
    MASCULINO("Masculino"),
    FEMININO("Feminino"),
    OUTRO("Outro"),
    NAO_INFORMAR("Prefiro não informar");

    private final String descricao;

    Genero(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}