package com.gestaoescolar.model;

import com.gestaoescolar.model.enums.NivelEscolar;
import com.gestaoescolar.model.enums.Serie;
import com.gestaoescolar.model.enums.Turno;
import com.gestaoescolar.model.enums.SerieHelper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "turmas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ano_letivo_id", "serie", "nome_turma", "turno"})
})
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da turma é obrigatório")
    @Size(min = 1, max = 20, message = "Nome da turma deve ter entre 1 e 20 caracteres")
    @Column(name = "nome_turma", nullable = false)
    private String nomeTurma;

    @NotNull(message = "Série é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Serie serie;

    @NotNull(message = "Nível escolar é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelEscolar nivel;

    @NotNull(message = "Turno é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Turno turno;

    @NotBlank(message = "Código da turma é obrigatório")
    @Size(max = 20)
    @Column(unique = true, nullable = false)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ano_letivo_id", nullable = false)
    @NotNull(message = "Ano letivo é obrigatório")
    private AnoLetivo anoLetivo;

    private Integer capacidade;

    @Size(max = 10)
    private String sala;

    private boolean ativa = true;

    private Integer vagasDisponiveis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_titular_id")
    private Professor professorTitular;

    // Construtores

    public Professor getProfessorTitular() {
        return professorTitular;
    }

    public void setProfessorTitular(Professor professorTitular) {
        this.professorTitular = professorTitular;
    }


    public Turma() {}

    public Turma(String nomeTurma, Serie serie, Turno turno, AnoLetivo anoLetivo) {
        this.nomeTurma = nomeTurma;
        this.serie = serie;
        this.turno = turno;
        this.anoLetivo = anoLetivo;
        this.nivel = SerieHelper.getNivelPorSerie(serie);
        gerarCodigoAutomatico();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) {
        this.nomeTurma = nomeTurma;
        gerarCodigoAutomatico();
    }

    public Serie getSerie() { return serie; }
    public void setSerie(Serie serie) {
        this.serie = serie;
        this.nivel = SerieHelper.getNivelPorSerie(serie);
        gerarCodigoAutomatico();
    }

    public NivelEscolar getNivel() { return nivel; }
    public void setNivel(NivelEscolar nivel) { this.nivel = nivel; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) {
        this.turno = turno;
        gerarCodigoAutomatico();
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public AnoLetivo getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(AnoLetivo anoLetivo) {
        this.anoLetivo = anoLetivo;
        gerarCodigoAutomatico();
    }

    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
        calcularVagasDisponiveis();
    }

    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public Integer getVagasDisponiveis() { return vagasDisponiveis; }
    public void setVagasDisponiveis(Integer vagasDisponiveis) { this.vagasDisponiveis = vagasDisponiveis; }

    // Métodos de negócio
    public void gerarCodigoAutomatico() {
        if (anoLetivo != null && serie != null && nomeTurma != null && turno != null) {
            String ano = String.valueOf(anoLetivo.getAno());
            String serieCodigo = serie.getNome().replace("º", "").replace("ª", "").substring(0, 1);
            String turnoCodigo = turno.name().substring(0, 1);

            this.codigo = String.format("%s-%s%s-%s", ano, serieCodigo, nomeTurma, turnoCodigo);
        }
    }

    private void calcularVagasDisponiveis() {
        // Futuro: calcular baseado em alunos matriculados
        this.vagasDisponiveis = this.capacidade;
    }

    public String getDescricaoCompleta() {
        return String.format("%s - %s (%s - %s)",
                serie.getNome(), nomeTurma, turno.getDescricao(), anoLetivo.getAno());
    }

    public boolean temVagas() {
        return vagasDisponiveis == null || vagasDisponiveis > 0;
    }

    public void ocuparVaga() {
        if (vagasDisponiveis != null && vagasDisponiveis > 0) {
            vagasDisponiveis--;
        }
    }

    public void liberarVaga() {
        if (vagasDisponiveis != null && capacidade != null && vagasDisponiveis < capacidade) {
            vagasDisponiveis++;
        }
    }

    @PrePersist
    @PreUpdate
    private void validarECalcular() {
        gerarCodigoAutomatico();
        calcularVagasDisponiveis();
    }

    @Override
    public String toString() {
        return getDescricaoCompleta();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Turma turma = (Turma) o;
        return id != null && id.equals(turma.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}