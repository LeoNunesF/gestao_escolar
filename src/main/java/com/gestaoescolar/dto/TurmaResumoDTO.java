package com.gestaoescolar.dto;

public class TurmaResumoDTO {
    private final String codigo;
    private final String nome;

    public TurmaResumoDTO(String codigo, String nome) {
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