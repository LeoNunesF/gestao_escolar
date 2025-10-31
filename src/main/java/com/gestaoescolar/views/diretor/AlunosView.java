package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Aluno;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.auth.AuthService;
import com.gestaoescolar.service.escola.AlunoService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Route(value = "diretor/alunos", layout = MainLayout.class)
@PageTitle("Gestão de Alunos")
public class AlunosView extends VerticalLayout {

    private final AlunoService alunoService;
    private final AuthService authService;
    private final Usuario usuarioLogado;
    private final com.gestaoescolar.service.escola.EnrollmentService enrollmentService;
    private final com.gestaoescolar.service.escola.TurmaService turmaService;

    private final Grid<Aluno> grid = new Grid<>(Aluno.class, false);
    private AlunoForm form;

    private final TextField filtro = new TextField();
    private final ComboBox<String> filtroStatus = new ComboBox<>();

    public AlunosView(AlunoService alunoService,
                      AuthService authService,
                      com.gestaoescolar.service.escola.EnrollmentService enrollmentService,
                      com.gestaoescolar.service.escola.TurmaService turmaService) {
        this.alunoService = alunoService;
        this.authService = authService;
        this.enrollmentService = enrollmentService;
        this.turmaService = turmaService;
        this.usuarioLogado = authService.getUsuarioLogado();

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.START); // conteúdo à esquerda
        add(new H2("Gestão de Alunos"));

        configureGrid();
        configureForm();
        add(createToolbar(), grid, form);

        updateList();
        closeEditor();
    }

    private HorizontalLayout createToolbar() {
        filtro.setPlaceholder("Filtrar por nome ou CPF");
        filtro.setClearButtonVisible(true);
        filtro.addValueChangeListener(e -> applyFilters());

        filtroStatus.setItems("Todos", "Ativos", "Inativos");
        filtroStatus.setValue("Todos");
        filtroStatus.addValueChangeListener(e -> applyFilters());

        Button novoAluno = new Button("Novo Aluno", e -> {
            Aluno novo = new Aluno();
            form.setAluno(novo);
            form.setVisible(true);
        });

        HorizontalLayout toolbar = new HorizontalLayout(filtro, filtroStatus, novoAluno);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return toolbar;
    }

    private void applyFilters() {
        String termo = filtro.getValue();
        String status = filtroStatus.getValue();

        List<Aluno> lista = alunoService.listStudents(usuarioLogado);
        if (lista == null) {
            grid.setItems(Collections.emptyList());
            return;
        }

        if (termo != null && !termo.isBlank()) {
            String t = termo.trim();
            String digits = t.replaceAll("\\D", "");
            if (digits.length() == 11) {
                var a = alunoService.findStudentByCpf(digits, usuarioLogado);
                lista = a != null ? List.of(a) : Collections.emptyList();
            } else {
                lista = alunoService.searchStudentsByName(t, usuarioLogado);
            }
        }

        if ("Ativos".equals(status)) {
            lista = lista.stream().filter(Aluno::isAtivo).toList();
        } else if ("Inativos".equals(status)) {
            lista = lista.stream().filter(a -> !a.isAtivo()).toList();
        }

        grid.setItems(lista);
    }

    // SUBSTITUA APENAS O MÉTODO configureGrid() PELO BLOCO ABAIXO
    private void configureGrid() {
        grid.addColumn(Aluno::getNomeCompleto).setHeader("Nome").setAutoWidth(true);
        grid.addColumn(Aluno::getCpf).setHeader("CPF").setAutoWidth(true);
        grid.addColumn(a -> a.getDataNascimento() != null ? a.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")
                .setHeader("Nascimento").setAutoWidth(true);
        grid.addColumn(a -> a.isAtivo() ? "Sim" : "Não").setHeader("Ativo").setAutoWidth(true);

        grid.addComponentColumn(a -> {
            Button editar = new Button("Editar", ev -> editAluno(a));
            Button matricular = new Button("Matricular", ev -> {
                MatricularAlunoDialog dlg = new MatricularAlunoDialog(
                        // injete os serviços necessários
                        // enrollmentService: precisamos referenciar via construtor da view
                        // turmaService: idem
                        // usuarioLogado: já temos na view
                        enrollmentService, // ver observação abaixo
                        turmaService,      // ver observação abaixo
                        usuarioLogado,
                        a.getId(),
                        this::updateList
                );
                dlg.open();
            });
            Button toggle = new Button(a.isAtivo() ? "Desativar" : "Reativar", ev -> {
                try {
                    if (a.isAtivo()) alunoService.deactivateStudent(a.getId(), usuarioLogado);
                    else alunoService.reactivateStudent(a.getId(), usuarioLogado);
                    updateList();
                } catch (Exception ex) {
                    Notification.show("Erro ao alterar status: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });
            return new HorizontalLayout(editar, matricular, toggle);
        }).setHeader("Ações").setAutoWidth(true);

        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(event -> editAluno(event.getValue()));
    }

    private void configureForm() {
        form = new AlunoForm(alunoService, usuarioLogado);
        form.setWidthFull(); // ocupa a largura do container
        form.getStyle().set("max-width", "980px"); // mas limita a largura máxima
        form.addSaveListener(this::salvarAluno);
        form.addCloseListener(e -> closeEditor());
    }

    private void salvarAluno(AlunoForm.SaveEvent event) {
        try {
            Aluno a = event.getAluno();
            if (a.getId() == null) {
                alunoService.createStudent(a, usuarioLogado);
                Notification.show("Aluno criado com sucesso.", 3000, Notification.Position.BOTTOM_START);
            } else {
                alunoService.updateStudent(a.getId(), a, usuarioLogado);
                Notification.show("Aluno atualizado com sucesso.", 3000, Notification.Position.BOTTOM_START);
            }
            updateList();
            closeEditor();
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido";
            Notification.show("Erro ao salvar: " + msg, 5000, Notification.Position.MIDDLE);
        }
    }

    private void editAluno(Aluno aluno) {
        if (aluno == null) {
            closeEditor();
        } else {
            form.setAluno(aluno);
            form.setVisible(true);
        }
    }

    private void closeEditor() {
        form.setAluno(null);
        form.setVisible(false);
        grid.getSelectionModel().deselectAll();
    }

    private void updateList() {
        grid.setItems(alunoService.listStudents(usuarioLogado));
    }
}