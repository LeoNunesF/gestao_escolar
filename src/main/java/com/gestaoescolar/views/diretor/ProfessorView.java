package com.gestaoescolar.views.diretor;

import com.gestaoescolar.dto.VinculoProfessorTurmaDTO;
import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.enums.FormacaoAcademica;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.auth.AuthService;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.service.escola.ProfessorTurmaService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Route(value = "diretor/professores", layout = MainLayout.class)
@PageTitle("Gestão de Professores")
public class ProfessorView extends VerticalLayout {

    private final ProfessorService professorService;
    private final ProfessorTurmaService professorTurmaService;
    private final AuthService authService;

    private Grid<Professor> grid = new Grid<>(Professor.class, false);
    private ProfessorForm form;
    private Usuario usuarioLogado;

    private TextField filtro = new TextField();
    private ComboBox<String> filtroStatus = new ComboBox<>();
    private ComboBox<FormacaoAcademica> filtroFormacao = new ComboBox<>();

    public ProfessorView(ProfessorService professorService,
                         ProfessorTurmaService professorTurmaService,
                         AuthService authService) {
        this.professorService = professorService;
        this.professorTurmaService = professorTurmaService;
        this.authService = authService;
        this.usuarioLogado = authService.getUsuarioLogado();

        setSizeFull();
        add(new H2("Gestão de Professores"));

        configureGrid();
        configureForm();
        HorizontalLayout toolbar = createToolbar();
        add(toolbar, grid, form);

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

        filtroFormacao.setItems(FormacaoAcademica.values());
        filtroFormacao.setPlaceholder("Todas formações");
        filtroFormacao.setClearButtonVisible(true);
        filtroFormacao.addValueChangeListener(e -> applyFilters());

        Button novoProfessor = new Button("Novo Professor", e -> {
            Professor novo = new Professor();
            form.setProfessor(novo);
            form.setVisible(true);
        });

        HorizontalLayout toolbar = new HorizontalLayout(filtro, filtroFormacao, filtroStatus, novoProfessor);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return toolbar;
    }

    private void applyFilters() {
        String termo = filtro.getValue();
        String status = filtroStatus.getValue();
        FormacaoAcademica formacao = filtroFormacao.getValue();

        List<Professor> lista = professorService.listarTodosProfessores(usuarioLogado);
        if (lista == null) {
            grid.setItems(Collections.emptyList());
            return;
        }

        List<Professor> filtrada = lista;

        if (termo != null && !termo.isBlank()) {
            String t = termo.trim().toLowerCase();
            String digits = t.replaceAll("\\D", "");
            if (digits.length() == 11) {
                var opt = professorService.buscarPorCpf(digits, usuarioLogado);
                filtrada = opt.map(List::of).orElseGet(Collections::emptyList);
            } else {
                filtrada = professorService.buscarPorNome(t, usuarioLogado);
            }
        }

        if ("Ativos".equals(status)) {
            filtrada = filtrada.stream().filter(Professor::isAtivo).collect(Collectors.toList());
        } else if ("Inativos".equals(status)) {
            filtrada = filtrada.stream().filter(p -> !p.isAtivo()).collect(Collectors.toList());
        }

        if (formacao != null) {
            filtrada = filtrada.stream()
                    .filter(p -> Objects.equals(p.getFormacao(), formacao))
                    .collect(Collectors.toList());
        }

        grid.setItems(filtrada);
    }

    private void configureGrid() {
        grid.removeAllColumns();
        grid.addColumn(Professor::getNomeCompleto).setHeader("Nome").setAutoWidth(true).setSortable(true);
        grid.addColumn(Professor::getCpf).setHeader("CPF").setAutoWidth(true).setSortable(true);;
        grid.addColumn(Professor::getEmail).setHeader("Email").setAutoWidth(true).setSortable(true);
        grid.addColumn(Professor::getTelefone).setHeader("Telefone").setAutoWidth(true).setSortable(true);
        grid.addColumn(Professor::getFormacao).setHeader("Formação").setAutoWidth(true).setSortable(true);
        grid.addColumn(prof -> prof.isAtivo() ? "Sim" : "Não").setHeader("Ativo").setAutoWidth(true);

        grid.addComponentColumn(prof -> {
            Button editar = new Button("Editar", ev -> editProfessor(prof));
            Button verTurmas = new Button("Ver Turmas", ev -> openTurmasDialog(prof));
            Button toggle = new Button(prof.isAtivo() ? "Desativar" : "Reativar", ev -> {
                try {
                    if (prof.isAtivo()) {
                        professorService.desativarProfessor(prof.getId(), usuarioLogado);
                    } else {
                        professorService.reativarProfessor(prof.getId(), usuarioLogado);
                    }
                    updateList();
                } catch (Exception ex) {
                    Notification.show("Erro ao alterar status: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });
            return new HorizontalLayout(editar, verTurmas, toggle);
        }).setHeader("Ações").setAutoWidth(true);

        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(event -> editProfessor(event.getValue()));
    }

    // exibir turma cadastradas por professor

    private void openTurmasDialog(Professor professor) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Turmas atribuídas a " + professor.getNomeCompleto());
        dialog.setWidth("900px");
        dialog.setHeight("70vh");
        dialog.setDraggable(true);
        dialog.setResizable(true);

        List<VinculoProfessorTurmaDTO> rows =
                professorTurmaService.listAssignmentSummariesByProfessor(professor.getId());

        if (rows == null || rows.isEmpty()) {
            dialog.add(new H3("Nenhuma turma atribuída."));
        } else {
            Grid<VinculoProfessorTurmaDTO> turmasGrid = new Grid<>(VinculoProfessorTurmaDTO.class, false);
            turmasGrid.addColumn(VinculoProfessorTurmaDTO::getCodigo).setHeader("Código").setAutoWidth(true);
            turmasGrid.addColumn(VinculoProfessorTurmaDTO::getNome).setHeader("Nome").setAutoWidth(true);
            turmasGrid.addColumn(dto -> mapPapel(dto.getPapel())).setHeader("Papel").setAutoWidth(true);
            turmasGrid.setSizeFull();
            turmasGrid.setItems(rows);
            dialog.add(turmasGrid);
        }

        // Footer com ações
        Button imprimir = new Button("Imprimir", e ->
                getUI().ifPresent(ui -> ui.getPage().executeJs("window.print()"))
        );
        Button fechar = new Button("Fechar", e -> dialog.close());

        dialog.getFooter().add(imprimir, fechar);
        dialog.open();
    }

    private String mapPapel(ProfessorTurma.Papel p) {
        if (p == null) return "";
        return switch (p) {
            case TITULAR -> "Titular";
            case SUBSTITUTO -> "Substituto";
            case COORDENADOR -> "Coordenador";
        };
    }

    private void configureForm() {
        form = new ProfessorForm(professorService, usuarioLogado);
        form.setWidth("30em");
        form.addListener(ProfessorForm.SaveEvent.class, this::salvarProfessor);
        form.addListener(ProfessorForm.CloseEvent.class, e -> closeEditor());
    }

    private void salvarProfessor(ProfessorForm.SaveEvent event) {
        try {
            Professor p = event.getProfessor();
            if (p.getId() == null) {
                professorService.criarProfessor(p, usuarioLogado);
                Notification.show("Professor criado com sucesso.", 3000, Notification.Position.BOTTOM_START);
            } else {
                professorService.atualizarProfessor(p.getId(), p, usuarioLogado);
                Notification.show("Professor atualizado com sucesso.", 3000, Notification.Position.BOTTOM_START);
            }
            updateList();
            closeEditor();
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido";
            Notification.show("Erro ao salvar: " + msg, 5000, Notification.Position.MIDDLE);
        }
    }

    private void editProfessor(Professor professor) {
        if (professor == null) {
            closeEditor();
        } else {
            form.setProfessor(professor);
            form.setVisible(true);
        }
    }

    private void closeEditor() {
        form.setProfessor(null);
        form.setVisible(false);
        grid.getSelectionModel().deselectAll();
    }

    private void updateList() {
        grid.setItems(professorService.listarTodosProfessores(usuarioLogado));
    }
}