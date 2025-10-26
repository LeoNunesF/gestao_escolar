package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.FormacaoAcademica;
import com.gestaoescolar.model.enums.Genero;
import com.gestaoescolar.model.enums.Serie;
import com.gestaoescolar.model.enums.Turno;
import com.gestaoescolar.model.enums.PerfilUsuario;
import com.gestaoescolar.model.enums.StatusAnoLetivo;
import com.gestaoescolar.repository.ProfessorRepository;
import com.gestaoescolar.repository.ProfessorTurmaRepository;
import com.gestaoescolar.repository.TurmaRepository;
import com.gestaoescolar.repository.AnoLetivoRepository;
import com.gestaoescolar.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProfessorTurmaServiceTest {

    @Autowired
    private ProfessorTurmaService professorTurmaService;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private ProfessorTurmaRepository professorTurmaRepository;

    @Autowired
    private AnoLetivoRepository anoLetivoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Professor professorTeste;
    private Turma turmaTeste;
    private Usuario usuarioLogado;
    private AnoLetivo anoLetivoTeste;

    @BeforeEach
    void setUp() {
        // Criar usuário administrativo para os testes
        usuarioLogado = new Usuario();
        usuarioLogado.setLogin("admin_test");
        usuarioLogado.setSenha("senha123");
        usuarioLogado.setEmail("admin_test@escola.com");
        usuarioLogado.setNomeCompleto("Admin Teste");
        usuarioLogado.setPerfil(PerfilUsuario.DIRETOR);
        usuarioLogado = usuarioRepository.save(usuarioLogado);

        // Criar ano letivo para a turma
        anoLetivoTeste = new AnoLetivo();
        anoLetivoTeste.setAno(2024);
        anoLetivoTeste.setDataInicio(LocalDate.of(2024, 2, 1));
        anoLetivoTeste.setDataTermino(LocalDate.of(2024, 12, 20));
        anoLetivoTeste.setStatus(StatusAnoLetivo.EM_ANDAMENTO);
        anoLetivoTeste = anoLetivoRepository.save(anoLetivoTeste);

        // Criar professor temporário
        professorTeste = new Professor();
        professorTeste.setNomeCompleto("Prof. Teste da Silva");
        professorTeste.setCpf("12345678901");
        professorTeste.setRg("123456789");
        professorTeste.setEmail("prof.teste@escola.com");
        professorTeste.setTelefone("(11) 99999-9999");
        professorTeste.setDataNascimento(LocalDate.of(1980, 1, 1));
        professorTeste.setGenero(Genero.MASCULINO);
        professorTeste.setFormacao(FormacaoAcademica.GRADUACAO_COMPLETA);
        professorTeste.setAtivo(true);
        professorTeste = professorRepository.save(professorTeste);

        // Criar turma temporária
        turmaTeste = new Turma();
        turmaTeste.setNomeTurma("A");
        turmaTeste.setSerie(Serie.PRIMEIRO_ANO);
        turmaTeste.setTurno(Turno.MANHA);
        turmaTeste.setAnoLetivo(anoLetivoTeste);
        turmaTeste.setCapacidade(30);
        turmaTeste.setAtiva(true);
        turmaTeste = turmaRepository.save(turmaTeste);
    }

    @AfterEach
    void tearDown() {
        // Limpar dados criados durante os testes
        if (professorTeste != null && professorTeste.getId() != null) {
            // Remover atribuições primeiro
            List<ProfessorTurma> atribuicoes = professorTurmaRepository.findByProfessorId(professorTeste.getId());
            professorTurmaRepository.deleteAll(atribuicoes);
            
            professorRepository.deleteById(professorTeste.getId());
        }

        if (turmaTeste != null && turmaTeste.getId() != null) {
            turmaRepository.deleteById(turmaTeste.getId());
        }

        if (anoLetivoTeste != null && anoLetivoTeste.getId() != null) {
            anoLetivoRepository.deleteById(anoLetivoTeste.getId());
        }

        if (usuarioLogado != null && usuarioLogado.getId() != null) {
            usuarioRepository.deleteById(usuarioLogado.getId());
        }
    }

    @Test
    void testAssignProfessorToTurma() {
        // Atribuir professor à turma
        ProfessorTurma atribuicao = professorTurmaService.assignProfessorToTurma(
                professorTeste.getId(),
                turmaTeste.getId(),
                "Titular",
                "Matemática",
                usuarioLogado
        );

        // Validar que a atribuição foi criada
        assertNotNull(atribuicao);
        assertNotNull(atribuicao.getId());
        assertEquals(professorTeste.getId(), atribuicao.getProfessor().getId());
        assertEquals(turmaTeste.getId(), atribuicao.getTurma().getId());
        assertEquals("Titular", atribuicao.getPapel());
        assertEquals("Matemática", atribuicao.getDisciplina());
        assertNotNull(atribuicao.getDataInicio());
        assertNull(atribuicao.getDataTermino());
    }

    @Test
    void testListByProfessor() {
        // Atribuir professor à turma
        professorTurmaService.assignProfessorToTurma(
                professorTeste.getId(),
                turmaTeste.getId(),
                usuarioLogado
        );

        // Buscar turmas do professor
        List<ProfessorTurma> turmasDoProfessor = professorTurmaService.listByProfessor(professorTeste.getId());

        // Validar resultado
        assertNotNull(turmasDoProfessor);
        assertEquals(1, turmasDoProfessor.size());
        assertEquals(turmaTeste.getId(), turmasDoProfessor.get(0).getTurma().getId());
    }

    @Test
    void testListByTurma() {
        // Atribuir professor à turma
        professorTurmaService.assignProfessorToTurma(
                professorTeste.getId(),
                turmaTeste.getId(),
                usuarioLogado
        );

        // Buscar professores da turma
        List<ProfessorTurma> professoresDaTurma = professorTurmaService.listByTurma(turmaTeste.getId());

        // Validar resultado
        assertNotNull(professoresDaTurma);
        assertEquals(1, professoresDaTurma.size());
        assertEquals(professorTeste.getId(), professoresDaTurma.get(0).getProfessor().getId());
    }

    @Test
    void testAssignProfessorToTurmaDuplicate() {
        // Atribuir professor à turma pela primeira vez
        professorTurmaService.assignProfessorToTurma(
                professorTeste.getId(),
                turmaTeste.getId(),
                usuarioLogado
        );

        // Tentar atribuir novamente (deve lançar exceção)
        assertThrows(IllegalArgumentException.class, () -> {
            professorTurmaService.assignProfessorToTurma(
                    professorTeste.getId(),
                    turmaTeste.getId(),
                    usuarioLogado
            );
        });
    }

    @Test
    void testRemoveAssignment() {
        // Atribuir professor à turma
        ProfessorTurma atribuicao = professorTurmaService.assignProfessorToTurma(
                professorTeste.getId(),
                turmaTeste.getId(),
                usuarioLogado
        );

        // Remover atribuição (soft delete)
        professorTurmaService.removeAssignment(atribuicao.getId(), usuarioLogado);

        // Verificar que a data de término foi definida
        ProfessorTurma atribuicaoAtualizada = professorTurmaRepository.findById(atribuicao.getId()).orElse(null);
        assertNotNull(atribuicaoAtualizada);
        assertNotNull(atribuicaoAtualizada.getDataTermino());
        assertFalse(atribuicaoAtualizada.isAtivo());
    }

    @Test
    void testIsProfessorAssignedToTurma() {
        // Verificar que inicialmente não há atribuição
        assertFalse(professorTurmaService.isProfessorAssignedToTurma(professorTeste.getId(), turmaTeste.getId()));

        // Atribuir professor à turma
        professorTurmaService.assignProfessorToTurma(
                professorTeste.getId(),
                turmaTeste.getId(),
                usuarioLogado
        );

        // Verificar que agora há atribuição
        assertTrue(professorTurmaService.isProfessorAssignedToTurma(professorTeste.getId(), turmaTeste.getId()));
    }
}
