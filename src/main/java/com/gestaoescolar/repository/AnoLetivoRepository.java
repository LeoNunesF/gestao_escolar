package com.gestaoescolar.repository;

import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.enums.StatusAnoLetivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnoLetivoRepository extends JpaRepository<AnoLetivo, Long> {

    Optional<AnoLetivo> findByAno(Integer ano);

    List<AnoLetivo> findByStatus(StatusAnoLetivo status);

    @Query("SELECT a FROM AnoLetivo a WHERE a.status = 'EM_ANDAMENTO'")
    Optional<AnoLetivo> findAnoLetivoAtivo();

    boolean existsByAno(Integer ano);

    List<AnoLetivo> findAllByOrderByAnoDesc();
}