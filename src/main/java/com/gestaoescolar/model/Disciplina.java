package com.gestaoescolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "disciplina", indexes = {
        @Index(name = "idx_disciplina_codigo", columnList = "codigo"),
        @Index(name = "idx_disciplina_nome", columnList = "nome")
})
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(length = 200, nullable = false)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    // carga horaria opcional (por semestre/ano)
    private Integer cargaHoraria;

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }
}