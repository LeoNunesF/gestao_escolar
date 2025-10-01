package com.gestaoescolar.repository;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.enums.FormacaoAcademica;
import com.gestaoescolar.model.enums.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    // Buscar professor por CPF
    Optional<Professor> findByCpf(String cpf);

    // Buscar professor por email
    Optional<Professor> findByEmail(String email);

    // Verificar se existe professor com CPF
    boolean existsByCpf(String cpf);

    // Verificar se existe professor com email
    boolean existsByEmail(String email);

    // Buscar professores ativos
    List<Professor> findByAtivoTrue();

    // Buscar professores inativos
    List<Professor> findByAtivoFalse();

    // Buscar professores por gênero
    List<Professor> findByGenero(Genero genero);

    // Buscar professores por formação acadêmica
    List<Professor> findByFormacao(FormacaoAcademica formacao);

    // Buscar professores com especialização
    @Query("SELECT p FROM Professor p WHERE p.especializacao IS NOT NULL AND p.especializacao != '' AND p.ativo = true")
    List<Professor> findProfessoresComEspecializacao();

    // Buscar professores admitidos após uma data
    List<Professor> findByDataAdmissaoAfter(LocalDate data);

    // Buscar professores admitidos antes de uma data
    List<Professor> findByDataAdmissaoBefore(LocalDate data);

    // Buscar professores que ainda não foram demitidos
    @Query("SELECT p FROM Professor p WHERE p.dataDemissao IS NULL AND p.ativo = true")
    List<Professor> findProfessoresAtivosSemDemissao();

    // Buscar professores por cidade
    @Query("SELECT p FROM Professor p WHERE p.endereco.cidade = :cidade AND p.ativo = true")
    List<Professor> findByCidade(@Param("cidade") String cidade);

    // Buscar professores por estado
    @Query("SELECT p FROM Professor p WHERE p.endereco.estado = :estado AND p.ativo = true")
    List<Professor> findByEstado(@Param("estado") String estado);

    // Buscar professores com formação superior
    @Query("SELECT p FROM Professor p WHERE p.formacao IN :formacoesSuperiores AND p.ativo = true")
    List<Professor> findProfessoresComFormacaoSuperior(@Param("formacoesSuperiores") List<FormacaoAcademica> formacoesSuperiores);

    // Contar professores ativos
    long countByAtivoTrue();

    // Contar professores por gênero
    long countByGenero(Genero genero);

    // Contar professores por formação
    long countByFormacao(FormacaoAcademica formacao);

    // Buscar professores ordenados por nome
    List<Professor> findAllByOrderByNomeCompleto();

    // Buscar professores ativos ordenados por data de admissão
    List<Professor> findByAtivoTrueOrderByDataAdmissaoDesc();

    // Buscar professores por parte do nome
    @Query("SELECT p FROM Professor p WHERE LOWER(p.nomeCompleto) LIKE LOWER(CONCAT('%', :nome, '%')) AND p.ativo = true")
    List<Professor> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    // Buscar professores com tempo de serviço maior que X anos
    @Query("SELECT p FROM Professor p WHERE p.dataAdmissao <= :dataLimite AND (p.dataDemissao IS NULL OR p.dataDemissao > :dataLimite) AND p.ativo = true")
    List<Professor> findProfessoresComTempoServicoMaiorQue(@Param("dataLimite") LocalDate dataLimite);
}