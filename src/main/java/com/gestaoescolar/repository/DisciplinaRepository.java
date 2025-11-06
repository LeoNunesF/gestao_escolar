package com.gestaoescolar.repository;

import com.gestaoescolar.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    Optional<Disciplina> findByCodigo(String codigo);
    // NOVO: busca/checagem ignorando maiúsculas/minúsculas
    Optional<Disciplina> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);
}