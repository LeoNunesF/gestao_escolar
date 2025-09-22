package com.gestaoescolar.service;

import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.enums.StatusAnoLetivo;
import com.gestaoescolar.repository.AnoLetivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AnoLetivoService {

    private final AnoLetivoRepository anoLetivoRepository;

    public AnoLetivoService(AnoLetivoRepository anoLetivoRepository) {
        this.anoLetivoRepository = anoLetivoRepository;
    }

    public List<AnoLetivo> findAll() {
        return anoLetivoRepository.findAllByOrderByAnoDesc();
    }

    public Optional<AnoLetivo> findById(Long id) {
        return anoLetivoRepository.findById(id);
    }

    public Optional<AnoLetivo> findByAno(Integer ano) {
        return anoLetivoRepository.findByAno(ano);
    }

    public Optional<AnoLetivo> findAnoLetivoAtivo() {
        return anoLetivoRepository.findAnoLetivoAtivo();
    }

    @Transactional
    public AnoLetivo save(AnoLetivo anoLetivo) {
        // Validação: não pode existir outro ano com o mesmo número
        if (anoLetivo.getId() == null && anoLetivoRepository.existsByAno(anoLetivo.getAno())) {
            throw new IllegalArgumentException("Já existe um ano letivo com este ano");
        }

        // Validação: data término deve ser após data início
        if (anoLetivo.getDataTermino().isBefore(anoLetivo.getDataInicio())) {
            throw new IllegalArgumentException("Data de término deve ser após data de início");
        }

        return anoLetivoRepository.save(anoLetivo);
    }

    @Transactional
    public void delete(Long id) {
        AnoLetivo anoLetivo = anoLetivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ano letivo não encontrado"));

        // Não permite excluir anos em andamento ou concluídos
        if (anoLetivo.getStatus() == StatusAnoLetivo.EM_ANDAMENTO ||
                anoLetivo.getStatus() == StatusAnoLetivo.CONCLUIDO) {
            throw new IllegalArgumentException("Não é possível excluir anos letivos em andamento ou concluídos");
        }

        anoLetivoRepository.delete(anoLetivo);
    }

    @Transactional
    public void ativarAnoLetivo(Long id) {
        // Primeiro desativa todos os outros
        List<AnoLetivo> anosAtivos = anoLetivoRepository.findByStatus(StatusAnoLetivo.EM_ANDAMENTO);
        for (AnoLetivo ano : anosAtivos) {
            ano.setStatus(StatusAnoLetivo.PLANEJAMENTO);
            anoLetivoRepository.save(ano);
        }

        // Ativa o ano selecionado
        AnoLetivo anoLetivo = anoLetivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ano letivo não encontrado"));

        anoLetivo.setStatus(StatusAnoLetivo.EM_ANDAMENTO);
        anoLetivoRepository.save(anoLetivo);
    }

    public boolean existsByAno(Integer ano) {
        return anoLetivoRepository.existsByAno(ano);
    }
}