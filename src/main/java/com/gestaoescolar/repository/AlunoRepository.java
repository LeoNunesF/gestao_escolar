package com.gestaoescolar.repository;

import com.gestaoescolar.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    List<Aluno> findAllByOrderByNomeCompletoAsc();
    List<Aluno> findByNomeCompletoContainingIgnoreCase(String nome);
    Optional<Aluno> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}