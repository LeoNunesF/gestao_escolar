package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.Serie;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grade_curricular", indexes = {
        @Index(name = "idx_grade_nome", columnList = "nome")
})
public class GradeCurricular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    private Serie serie; // opcional (pode ser uma grade “genérica”)

    @OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GradeCurricularItem> itens = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    public List<GradeCurricularItem> getItens() {
        return itens;
    }

    public void setItens(List<GradeCurricularItem> itens) {
        this.itens = itens;
    }
}