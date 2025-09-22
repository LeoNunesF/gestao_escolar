package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.StatusAnoLetivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "ano_letivo")
public class AnoLetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Ano é obrigatório")
    @Column(unique = true, nullable = false)
    private Integer ano;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;

    @NotNull(message = "Data de término é obrigatória")
    private LocalDate dataTermino;

    @Enumerated(EnumType.STRING)
    private StatusAnoLetivo status = StatusAnoLetivo.PLANEJAMENTO;

    private String observacoes;

    // Construtores
    public AnoLetivo() {}

    public AnoLetivo(Integer ano, LocalDate dataInicio, LocalDate dataTermino) {
        this.ano = ano;
        this.dataInicio = dataInicio;
        this.dataTermino = dataTermino;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataTermino() { return dataTermino; }
    public void setDataTermino(LocalDate dataTermino) { this.dataTermino = dataTermino; }

    public StatusAnoLetivo getStatus() { return status; }
    public void setStatus(StatusAnoLetivo status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    // Métodos auxiliares
    public boolean isAtivo() {
        return StatusAnoLetivo.EM_ANDAMENTO.equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnoLetivo anoLetivo = (AnoLetivo) o;
        return Objects.equals(id, anoLetivo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AnoLetivo " + ano;
    }
}