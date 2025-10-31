package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.Genero;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aluno")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Situação
    @Column(nullable = false)
    private boolean ativo = true;

    // Documentos (flexível para RA/Certidão/CPF)
    private String docTipo;           // ex.: "CPF", "Certidão", "RA"
    private String docNumero;
    @Column(unique = true)
    private String cpf;               // opcional; validar quando informado
    private String inep;              // opcional
    private String nis;               // opcional
    private String justificativaDocumentos;

    // Dados pessoais
    @Column(nullable = false)
    private String nomeCompleto;
    private String nomeSocial;
    @Column(nullable = false)
    private LocalDate dataNascimento;
    @Enumerated(EnumType.STRING)
    private Genero genero;
    private String corRaca;

    // Endereço
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;

    // Contato
    private String telefone;
    private String email;

    // Saúde básica
    private String alergias;
    private String observacoesSaude;

    // Observações gerais
    @Column(length = 4000)
    private String observacoes;

    // Responsáveis (vínculo)
    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlunoResponsavel> responsaveis = new ArrayList<>();

    // Auditoria simples
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters e setters

    public Long getId() {
        return id;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getDocTipo() {
        return docTipo;
    }

    public void setDocTipo(String docTipo) {
        this.docTipo = docTipo;
    }

    public String getDocNumero() {
        return docNumero;
    }

    public void setDocNumero(String docNumero) {
        this.docNumero = docNumero;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getInep() {
        return inep;
    }

    public void setInep(String inep) {
        this.inep = inep;
    }

    public String getNis() {
        return nis;
    }

    public void setNis(String nis) {
        this.nis = nis;
    }

    public String getJustificativaDocumentos() {
        return justificativaDocumentos;
    }

    public void setJustificativaDocumentos(String justificativaDocumentos) {
        this.justificativaDocumentos = justificativaDocumentos;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getNomeSocial() {
        return nomeSocial;
    }

    public void setNomeSocial(String nomeSocial) {
        this.nomeSocial = nomeSocial;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public String getCorRaca() {
        return corRaca;
    }

    public void setCorRaca(String corRaca) {
        this.corRaca = corRaca;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getObservacoesSaude() {
        return observacoesSaude;
    }

    public void setObservacoesSaude(String observacoesSaude) {
        this.observacoesSaude = observacoesSaude;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<AlunoResponsavel> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(List<AlunoResponsavel> responsaveis) {
        this.responsaveis = responsaveis;
    }
}