package com.gestaoescolar.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Embeddable
public class Endereco {

    @NotBlank(message = "Logradouro é obrigatório")
    @Size(max = 100)
    private String logradouro;

    @NotBlank(message = "Número é obrigatório")
    @Size(max = 10)
    private String numero;

    @Size(max = 50)
    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 50)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatório")
    @Size(max = 50)
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
    private String estado;

    @NotBlank(message = "CEP é obrigatório")
    @Size(min = 8, max = 9, message = "CEP deve ter 8 ou 9 caracteres")
    private String cep;

    // Construtores
    public Endereco() {}

    public Endereco(String logradouro, String numero, String bairro, String cidade, String estado, String cep) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    // Getters e Setters
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getEnderecoCompleto() {
        return String.format("%s, %s%s - %s, %s - %s",
                logradouro, numero,
                complemento != null ? " " + complemento : "",
                bairro, cidade, estado);
    }

    @Override
    public String toString() {
        return getEnderecoCompleto();
    }
}