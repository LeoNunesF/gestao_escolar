package com.gestaoescolar.model.enums;

public enum Serie {
    // Educação Infantil
    MATERNAL_I("Maternal I"),
    MATERNAL_II("Maternal II"),
    MATERNAL_III("Maternal III"),
    PRE_I("Pré I"),
    PRE_II("Pré II"),

    // Fundamental I
    PRIMEIRO_ANO("1º Ano"),
    SEGUNDO_ANO("2º Ano"),
    TERCEIRO_ANO("3º Ano"),
    QUARTO_ANO("4º Ano"),
    QUINTO_ANO("5º Ano"),

    // Fundamental II
    SEXTO_ANO("6º Ano"),
    SETIMO_ANO("7º Ano"),
    OITAVO_ANO("8º Ano"),
    NONO_ANO("9º Ano"),

    // Ensino Médio
    PRIMEIRA_SERIE("1ª Série"),
    SEGUNDA_SERIE("2ª Série"),
    TERCEIRA_SERIE("3ª Série");

    private final String nome;

    Serie(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    // Método auxiliar para buscar por nome
    public static Serie fromNome(String nome) {
        for (Serie serie : values()) {
            if (serie.nome.equals(nome)) {
                return serie;
            }
        }
        throw new IllegalArgumentException("Série não encontrada: " + nome);
    }

    // Método para obter todas as séries de um nível
    public static Serie[] getSeriesPorNivel(NivelEscolar nivel) {
        return switch (nivel) {
            case EDUCACAO_INFANTIL -> new Serie[]{
                    MATERNAL_I, MATERNAL_II, MATERNAL_III, PRE_I, PRE_II
            };
            case FUNDAMENTAL_I -> new Serie[]{
                    PRIMEIRO_ANO, SEGUNDO_ANO, TERCEIRO_ANO, QUARTO_ANO, QUINTO_ANO
            };
            case FUNDAMENTAL_II -> new Serie[]{
                    SEXTO_ANO, SETIMO_ANO, OITAVO_ANO, NONO_ANO
            };
            case ENSINO_MEDIO -> new Serie[]{
                    PRIMEIRA_SERIE, SEGUNDA_SERIE, TERCEIRA_SERIE
            };
        };
    }
}