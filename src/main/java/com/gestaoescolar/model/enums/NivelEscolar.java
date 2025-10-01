package com.gestaoescolar.model.enums;

public enum NivelEscolar {
    EDUCACAO_INFANTIL("Educação Infantil"),
    FUNDAMENTAL_I("Ensino Fundamental I"),
    FUNDAMENTAL_II("Ensino Fundamental II"),
    ENSINO_MEDIO("Ensino Médio");

    private final String descricao;

    NivelEscolar(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // Método para obter o nível a partir de uma série
    public static NivelEscolar getNivelPorSerie(Serie serie) {
        return switch(serie) {
            case MATERNAL_I, MATERNAL_II, MATERNAL_III, PRE_I, PRE_II
                    -> EDUCACAO_INFANTIL;
            case PRIMEIRO_ANO, SEGUNDO_ANO, TERCEIRO_ANO, QUARTO_ANO, QUINTO_ANO
                    -> FUNDAMENTAL_I;
            case SEXTO_ANO, SETIMO_ANO, OITAVO_ANO, NONO_ANO
                    -> FUNDAMENTAL_II;
            case PRIMEIRA_SERIE, SEGUNDA_SERIE, TERCEIRA_SERIE
                    -> ENSINO_MEDIO;
        };
    }
}