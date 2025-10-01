package com.gestaoescolar.repository;

import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.enums.NivelEscolar;
import com.gestaoescolar.model.enums.Serie;
import com.gestaoescolar.model.enums.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {

    // Buscar turmas por ano letivo
    List<Turma> findByAnoLetivo(AnoLetivo anoLetivo);

    // Buscar turmas por ano letivo e série
    List<Turma> findByAnoLetivoAndSerie(AnoLetivo anoLetivo, Serie serie);

    // Buscar turmas por ano letivo e nível
    List<Turma> findByAnoLetivoAndNivel(AnoLetivo anoLetivo, NivelEscolar nivel);

    // Buscar turmas por ano letivo e turno
    List<Turma> findByAnoLetivoAndTurno(AnoLetivo anoLetivo, Turno turno);

    // Buscar turmas ativas por ano letivo
    List<Turma> findByAnoLetivoAndAtivaTrue(AnoLetivo anoLetivo);

    // Buscar turmas inativas por ano letivo
    List<Turma> findByAnoLetivoAndAtivaFalse(AnoLetivo anoLetivo);

    // Buscar turma por código
    Optional<Turma> findByCodigo(String codigo);

    // Verificar se existe turma com mesmo nome/série/turno no mesmo ano
    boolean existsByAnoLetivoAndSerieAndNomeTurmaAndTurno(
            AnoLetivo anoLetivo, Serie serie, String nomeTurma, Turno turno);

    // Buscar turmas com vagas disponíveis
    @Query("SELECT t FROM Turma t WHERE t.vagasDisponiveis > 0 AND t.ativa = true")
    List<Turma> findTurmasComVagas();

    // Buscar turmas com vagas por ano letivo
    @Query("SELECT t FROM Turma t WHERE t.anoLetivo = :anoLetivo AND t.vagasDisponiveis > 0 AND t.ativa = true")
    List<Turma> findTurmasComVagasPorAnoLetivo(@Param("anoLetivo") AnoLetivo anoLetivo);

    // Contar turmas por ano letivo
    long countByAnoLetivo(AnoLetivo anoLetivo);

    // Contar turmas ativas por ano letivo
    long countByAnoLetivoAndAtivaTrue(AnoLetivo anoLetivo);

    // Buscar turmas por série
    List<Turma> findBySerie(Serie serie);

    // Buscar turmas por nível
    List<Turma> findByNivel(NivelEscolar nivel);

    // Buscar turmas por turno
    List<Turma> findByTurno(Turno turno);

    // Buscar todas as turmas ativas
    List<Turma> findByAtivaTrue();

    // Buscar turmas com capacidade maior que X
    @Query("SELECT t FROM Turma t WHERE t.capacidade > :capacidadeMinima AND t.ativa = true")
    List<Turma> findTurmasComCapacidadeMaiorQue(@Param("capacidadeMinima") Integer capacidadeMinima);

    // Buscar turmas ordenadas por série e nome
    @Query("SELECT t FROM Turma t WHERE t.anoLetivo = :anoLetivo ORDER BY t.serie, t.nomeTurma")
    List<Turma> findByAnoLetivoOrderBySerieAndNome(@Param("anoLetivo") AnoLetivo anoLetivo);

    // Buscar turmas por ano letivo com professor titular
    @Query("SELECT t FROM Turma t WHERE t.anoLetivo = :anoLetivo AND t.professorTitular IS NOT NULL")
    List<Turma> findByAnoLetivoComProfessorTitular(@Param("anoLetivo") AnoLetivo anoLetivo);

    // Buscar turmas sem professor titular
    @Query("SELECT t FROM Turma t WHERE t.professorTitular IS NULL AND t.ativa = true")
    List<Turma> findTurmasSemProfessorTitular();
}