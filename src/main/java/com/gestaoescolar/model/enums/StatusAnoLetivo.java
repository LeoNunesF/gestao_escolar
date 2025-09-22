package com.gestaoescolar.model.enums;

public enum StatusAnoLetivo {
    PLANEJAMENTO("Planejamento"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusAnoLetivo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}