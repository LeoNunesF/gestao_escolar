package com.gestaoescolar.model;

import java.time.LocalDate;

public class Aluno {
    private Long id;
    private String matricula;
    private String nome;
    private LocalDate dataNascimento;
    private String turma;
    private String responsavel;
    private String telefone;
    private String email;

    // Construtores
    public Aluno() {}

    public Aluno(String matricula, String nome, String turma, String responsavel, String telefone) {
        this.matricula = matricula;
        this.nome = nome;
        this.turma = turma;
        this.responsavel = responsavel;
        this.telefone = telefone;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getTurma() { return turma; }
    public void setTurma(String turma) { this.turma = turma; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}