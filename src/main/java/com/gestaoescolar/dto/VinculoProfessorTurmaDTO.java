package com.gestaoescolar.dto;

import com.gestaoescolar.model.ProfessorTurma;

public class VinculoProfessorTurmaDTO {
    private final String codigo;
    private final String nome;
    private final ProfessorTurma.Papel papel;

    public VinculoProfessorTurmaDTO(String codigo, String nome, ProfessorTurma.Papel papel) {
        this.codigo = codigo;
        this.nome = nome;
        this.papel = papel;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public ProfessorTurma.Papel getPapel() {
        return papel;
    }
}