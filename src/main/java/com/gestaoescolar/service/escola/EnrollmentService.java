package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.Aluno;
import com.gestaoescolar.model.Matricula;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.MatriculaStatus;
import com.gestaoescolar.repository.AlunoRepository;
import com.gestaoescolar.repository.MatriculaRepository;
import com.gestaoescolar.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EnrollmentService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;

    public EnrollmentService(MatriculaRepository matriculaRepository,
                             AlunoRepository alunoRepository,
                             TurmaRepository turmaRepository) {
        this.matriculaRepository = matriculaRepository;
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
    }

    private void requireAdmin(Usuario usuario) {
        if (usuario == null || !usuario.isAdministrativo()) {
            throw new SecurityException("Acesso restrito à administração");
        }
    }

    //
    @Transactional
    public Matricula enrollStudent(Long alunoId, Long turmaId, LocalDate dataInicio, Usuario usuario) {
        requireAdmin(usuario);
        if (dataInicio == null) {
            dataInicio = LocalDate.now();
        }

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado"));
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        // Exclusividade na MESMA turma
        if (matriculaRepository.existsByAlunoIdAndTurmaIdAndStatus(alunoId, turmaId, MatriculaStatus.ATIVA)) {
            throw new IllegalArgumentException("O aluno já possui matrícula ativa nesta turma.");
        }

        // Exclusividade no MESMO ANO LETIVO (bloqueia mais de uma turma ativa no mesmo ano)
        Long anoLetivoId = turma.getAnoLetivo() != null ? turma.getAnoLetivo().getId() : null;
        if (anoLetivoId != null) {
            boolean jaTemNoAno = matriculaRepository
                    .existsByAlunoIdAndTurma_AnoLetivo_IdAndStatus(alunoId, anoLetivoId, MatriculaStatus.ATIVA);
            if (jaTemNoAno) {
                // Mensagem amigável com ano e, se possível, turma atual
                Integer ano = turma.getAnoLetivo().getAno();
                String baseMsg = "O aluno já possui matrícula ativa no ano letivo " + (ano != null ? ano : "") + ".";
                var atualOpt = matriculaRepository
                        .findFirstByAlunoIdAndTurma_AnoLetivo_IdAndStatusOrderByIdDesc(alunoId, anoLetivoId, MatriculaStatus.ATIVA);
                if (atualOpt.isPresent() && atualOpt.get().getTurma() != null) {
                    var t = atualOpt.get().getTurma();
                    String codigo = t.getCodigo() != null ? t.getCodigo() : "";
                    String nome = t.getNomeTurma() != null ? t.getNomeTurma() : "";
                    throw new IllegalArgumentException(baseMsg + " (Turma " + codigo + (nome.isBlank() ? "" : " - " + nome) + ").");
                } else {
                    throw new IllegalArgumentException(baseMsg);
                }
            }
        }

        // Capacidade da turma (se informada)
        Integer capacidade = turma.getCapacidade();
        if (capacidade != null && capacidade > 0) {
            long ativas = matriculaRepository.countByTurmaIdAndStatus(turmaId, MatriculaStatus.ATIVA);
            if (ativas >= capacidade) {
                throw new IllegalArgumentException("Turma sem vagas disponíveis.");
            }
        }

        Matricula m = new Matricula();
        m.setAluno(aluno);
        m.setTurma(turma);
        m.setStatus(MatriculaStatus.ATIVA);
        m.setDataInicio(dataInicio);
        return matriculaRepository.save(m);
    }

    @Transactional
    public Matricula cancelEnrollment(Long matriculaId, LocalDate dataCancelamento, String motivo, Usuario usuario) {
        requireAdmin(usuario);
        Matricula m = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

        if (m.getStatus() != MatriculaStatus.ATIVA) {
            throw new IllegalArgumentException("Apenas matrículas ativas podem ser canceladas.");
        }

        m.setStatus(MatriculaStatus.CANCELADA);
        m.setDataTermino(dataCancelamento != null ? dataCancelamento : LocalDate.now());
        m.setMotivo(motivo);
        return matriculaRepository.save(m);
    }

    @Transactional
    public Matricula concludeEnrollment(Long matriculaId, LocalDate dataConclusao, Usuario usuario) {
        requireAdmin(usuario);
        Matricula m = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

        if (m.getStatus() != MatriculaStatus.ATIVA) {
            throw new IllegalArgumentException("Apenas matrículas ativas podem ser concluídas.");
        }

        m.setStatus(MatriculaStatus.CONCLUIDA);
        m.setDataTermino(dataConclusao != null ? dataConclusao : LocalDate.now());
        return matriculaRepository.save(m);
    }

    @Transactional
    public Matricula transferEnrollment(Long matriculaId, Long novaTurmaId, LocalDate dataTransferencia, String motivo, Usuario usuario) {
        requireAdmin(usuario);
        if (dataTransferencia == null) dataTransferencia = LocalDate.now();

        Matricula atual = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

        if (atual.getStatus() != MatriculaStatus.ATIVA) {
            throw new IllegalArgumentException("Apenas matrículas ativas podem ser transferidas.");
        }

        Turma novaTurma = turmaRepository.findById(novaTurmaId)
                .orElseThrow(() -> new IllegalArgumentException("Nova turma não encontrada"));

        // Capacidade na nova turma
        Integer capacidade = novaTurma.getCapacidade();
        if (capacidade != null && capacidade > 0) {
            long ativas = matriculaRepository.countByTurmaIdAndStatus(novaTurmaId, MatriculaStatus.ATIVA);
            if (ativas >= capacidade) {
                throw new IllegalArgumentException("Nova turma sem vagas disponíveis.");
            }
        }

        // Fechar matrícula atual
        atual.setStatus(MatriculaStatus.TRANSFERIDA);
        atual.setDataTermino(dataTransferencia.minusDays(1));
        atual.setMotivo(motivo);
        matriculaRepository.save(atual);

        // Abrir nova matrícula
        Matricula nova = new Matricula();
        nova.setAluno(atual.getAluno());
        nova.setTurma(novaTurma);
        nova.setStatus(MatriculaStatus.ATIVA);
        nova.setDataInicio(dataTransferencia);
        return matriculaRepository.save(nova);
    }

    public List<Matricula> listEnrollmentsByClass(Long turmaId, Usuario usuario) {
        requireAdmin(usuario);
        return matriculaRepository.findByTurmaId(turmaId);
    }

    public List<Matricula> listEnrollmentsByStudent(Long alunoId, Usuario usuario) {
        requireAdmin(usuario);
        return matriculaRepository.findByAlunoId(alunoId);
    }
}