package com.gestaoescolar.repository;

import com.gestaoescolar.model.Responsavel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResponsavelRepository extends JpaRepository<Responsavel, Long> {
    Optional<Responsavel> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}