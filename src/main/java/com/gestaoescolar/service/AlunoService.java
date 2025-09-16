package com.gestaoescolar.service;

import com.gestaoescolar.model.Aluno;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AlunoService {
    private final List<Aluno> alunos = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong(1);

    public AlunoService() {
        // Dados iniciais para teste
        alunos.add(new Aluno("20230001", "Ana Silva Oliveira", "7º A", "Maria Silva", "(11) 99999-9999"));
        alunos.add(new Aluno("20230002", "Carlos Eduardo Santos", "8º B", "João Santos", "(11) 98888-8888"));
        alunos.add(new Aluno("20230003", "Mariana Costa Pereira", "6º C", "Fernanda Costa", "(11) 97777-7777"));

        // Configurar IDs
        for (int i = 0; i < alunos.size(); i++) {
            alunos.get(i).setId((long) (i + 1));
        }
    }

    public List<Aluno> findAll() {
        return new ArrayList<>(alunos);
    }

    public Optional<Aluno> findById(Long id) {
        return alunos.stream().filter(aluno -> aluno.getId().equals(id)).findFirst();
    }

    public Aluno save(Aluno aluno) {
        if (aluno.getId() == null) {
            aluno.setId(counter.getAndIncrement());
            alunos.add(aluno);
        } else {
            delete(aluno.getId());
            alunos.add(aluno);
        }
        return aluno;
    }

    public void delete(Long id) {
        alunos.removeIf(aluno -> aluno.getId().equals(id));
    }

    public List<Aluno> findByNome(String nome) {
        if (nome == null || nome.isEmpty()) {
            return findAll();
        }
        String termo = nome.toLowerCase();
        return alunos.stream()
                .filter(aluno -> aluno.getNome().toLowerCase().contains(termo))
                .toList();
    }
}