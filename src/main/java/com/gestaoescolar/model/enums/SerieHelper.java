package com.gestaoescolar.model.enums;

public class SerieHelper {

    public static NivelEscolar getNivelPorSerie(Serie serie) {
        if (serie == null) {
            return null;
        }

        return switch(serie) {
            case MATERNAL_I, MATERNAL_II, MATERNAL_III, PRE_I, PRE_II
                    -> NivelEscolar.EDUCACAO_INFANTIL;
            case PRIMEIRO_ANO, SEGUNDO_ANO, TERCEIRO_ANO, QUARTO_ANO, QUINTO_ANO
                    -> NivelEscolar.FUNDAMENTAL_I;
            case SEXTO_ANO, SETIMO_ANO, OITAVO_ANO, NONO_ANO
                    -> NivelEscolar.FUNDAMENTAL_II;
            case PRIMEIRA_SERIE, SEGUNDA_SERIE, TERCEIRA_SERIE
                    -> NivelEscolar.ENSINO_MEDIO;
        };
    }

    public static Serie[] getSeriesPorNivel(NivelEscolar nivel) {
        if (nivel == null) {
            return new Serie[0];
        }

        return switch(nivel) {
            case EDUCACAO_INFANTIL -> new Serie[]{
                    Serie.MATERNAL_I, Serie.MATERNAL_II, Serie.MATERNAL_III,
                    Serie.PRE_I, Serie.PRE_II
            };
            case FUNDAMENTAL_I -> new Serie[]{
                    Serie.PRIMEIRO_ANO, Serie.SEGUNDO_ANO, Serie.TERCEIRO_ANO,
                    Serie.QUARTO_ANO, Serie.QUINTO_ANO
            };
            case FUNDAMENTAL_II -> new Serie[]{
                    Serie.SEXTO_ANO, Serie.SETIMO_ANO, Serie.OITAVO_ANO, Serie.NONO_ANO
            };
            case ENSINO_MEDIO -> new Serie[]{
                    Serie.PRIMEIRA_SERIE, Serie.SEGUNDA_SERIE, Serie.TERCEIRA_SERIE
            };
        };
    }

    public static String getDescricaoNivel(NivelEscolar nivel) {
        return nivel != null ? nivel.getDescricao() : "";
    }

    public static String getDescricaoSerie(Serie serie) {
        return serie != null ? serie.getNome() : "";
    }

    public static boolean isEducacaoInfantil(Serie serie) {
        return getNivelPorSerie(serie) == NivelEscolar.EDUCACAO_INFANTIL;
    }

    public static boolean isFundamentalI(Serie serie) {
        return getNivelPorSerie(serie) == NivelEscolar.FUNDAMENTAL_I;
    }

    public static boolean isFundamentalII(Serie serie) {
        return getNivelPorSerie(serie) == NivelEscolar.FUNDAMENTAL_II;
    }

    public static boolean isEnsinoMedio(Serie serie) {
        return getNivelPorSerie(serie) == NivelEscolar.ENSINO_MEDIO;
    }
}