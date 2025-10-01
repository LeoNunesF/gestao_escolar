package com.gestaoescolar.model;
import com.gestaoescolar.model.enums.Genero;
import com.gestaoescolar.model.enums.FormacaoAcademica;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "professores")
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 5, max = 100, message = "Nome deve ter entre 5 e 100 caracteres")
    @Column(nullable = false)
    private String nomeCompleto;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos")
    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    @NotBlank(message = "RG é obrigatório")
    @Column(nullable = false)
    private String rg;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Telefone deve seguir o padrão (11) 99999-9999")
    @Column(nullable = false)
    private String telefone;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Gênero é obrigatório")
    private Genero genero;

    @Embedded
    private Endereco endereco;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Formação acadêmica é obrigatória")
    private FormacaoAcademica formacao;

    @Size(max = 50)
    private String especializacao;

    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

    @Column(name = "data_demissao")
    private LocalDate dataDemissao;

    private boolean ativo = true;

    @Size(max = 500)
    private String observacoes;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    // Construtores
    public Professor() {}

    public Professor(String nomeCompleto, String cpf, String email, String telefone) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.dataAdmissao = LocalDate.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Genero getGenero() { return genero; }
    public void setGenero(Genero genero) { this.genero = genero; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public FormacaoAcademica getFormacao() { return formacao; }
    public void setFormacao(FormacaoAcademica formacao) { this.formacao = formacao; }

    public String getEspecializacao() { return especializacao; }
    public void setEspecializacao(String especializacao) { this.especializacao = especializacao; }

    public LocalDate getDataAdmissao() { return dataAdmissao; }
    public void setDataAdmissao(LocalDate dataAdmissao) { this.dataAdmissao = dataAdmissao; }

    public LocalDate getDataDemissao() { return dataDemissao; }
    public void setDataDemissao(LocalDate dataDemissao) { this.dataDemissao = dataDemissao; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    // Métodos de negócio
    public Integer getIdade() {
        if (dataNascimento == null) return null;
        return LocalDate.now().getYear() - dataNascimento.getYear();
    }

    public boolean isTrabalhando() {
        return ativo && dataAdmissao != null && dataDemissao == null;
    }

    public Long getTempoServicoMeses() {
        if (dataAdmissao == null) return null;

        LocalDate dataFinal = dataDemissao != null ? dataDemissao : LocalDate.now();
        return java.time.temporal.ChronoUnit.MONTHS.between(dataAdmissao, dataFinal);
    }

    public String getNomeFormatado() {
        return "Prof. " + nomeCompleto;
    }

    @PreUpdate
    private void atualizarDataModificacao() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return getNomeFormatado();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Professor professor = (Professor) o;
        return id != null && id.equals(professor.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}