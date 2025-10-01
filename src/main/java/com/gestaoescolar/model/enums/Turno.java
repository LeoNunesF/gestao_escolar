package com.gestaoescolar.model.enums;

public enum Turno {
    MANHA("Manhã"),
    TARDE("Tarde"),
    NOITE("Noite"),
    INTEGRAL("Integral");

    private final String descricao;

    Turno(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // Método auxiliar para buscar por descrição
    public static Turno fromDescricao(String descricao) {
        for (Turno turno : values()) {
            if (turno.descricao.equals(descricao)) {
                return turno;
            }
        }
        throw new IllegalArgumentException("Turno não encontrado: " + descricao);
    }
}