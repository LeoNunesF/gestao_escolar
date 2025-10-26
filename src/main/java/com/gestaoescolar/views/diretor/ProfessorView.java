package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Turma;
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

    // filtros
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

        // Cabeçalho
        add(new H2("Gestão de Professores"));

        // Configura componentes
        configureGrid();
        configureForm();

        // Monta toolbar (retorna o layout) e adiciona toolbar + grid + form ao layout
        HorizontalLayout toolbar = createToolbar();
        add(toolbar, grid, form);

        // Inicializa dados e estado do editor
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
            // cria e abre o form corretamente
            Professor novo = new Professor();
            form.setProfessor(novo);
            form.setVisible(true);
        });

        HorizontalLayout toolbar = new HorizontalLayout(filtro, filtroFormacao, filtroStatus, novoProfessor);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return toolbar;
    }

    /**
     * Aplica os filtros. Busca a lista completa do service e filtra em memória.
     */
    private void applyFilters() {
        String termo = filtro.getValue();
        String status = filtroStatus.getValue();
        FormacaoAcademica formacao = filtroFormacao.getValue();

        // Pega lista completa (o service já faz validação de permissão)
        List<Professor> lista = professorService.listarTodosProfessores(usuarioLogado);
        if (lista == null) {
            grid.setItems(Collections.emptyList());
            return;
        }

        List<Professor> filtrada = lista;

        if (termo != null && !termo.isBlank()) {
            String t = termo.trim().toLowerCase();
            String digits = t.replaceAll("\\D", "");

            // Se parece CPF (11 dígitos), tenta busca direta por CPF via service
            if (digits.length() == 11) {
                var opt = professorService.buscarPorCpf(digits, usuarioLogado);
                if (opt.isPresent()) {
                    filtrada = List.of(opt.get());
                } else {
                    filtrada = Collections.emptyList();
                }
            } else {
                // Busca por nome (usa o método do seu service)
                filtrada = professorService.buscarPorNome(t, usuarioLogado);
            }
        }

        // Filtrar por status (se selecionado)
        if ("Ativos".equals(status)) {
            filtrada = filtrada.stream().filter(Professor::isAtivo).collect(Collectors.toList());
        } else if ("Inativos".equals(status)) {
            filtrada = filtrada.stream().filter(p -> !p.isAtivo()).collect(Collectors.toList());
        }

        // Filtrar por formação (se selecionada)
        if (formacao != null) {
            filtrada = filtrada.stream()
                    .filter(p -> Objects.equals(p.getFormacao(), formacao))
                    .collect(Collectors.toList());
        }

        grid.setItems(filtrada);
    }

    private void configureGrid() {
        grid.removeAllColumns();
        // Ajuste as colunas aqui para refletir exatamente o que você quer ver.
        grid.addColumn(Professor::getNomeCompleto).setHeader("Nome").setAutoWidth(true);
        grid.addColumn(Professor::getCpf).setHeader("CPF").setAutoWidth(true);
        grid.addColumn(Professor::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Professor::getTelefone).setHeader("Telefone").setAutoWidth(true);
        grid.addColumn(Professor::getFormacao).setHeader("Formação").setAutoWidth(true);
        grid.addColumn(prof -> prof.isAtivo() ? "Sim" : "Não").setHeader("Ativo").setAutoWidth(true);

        // Ações: editar, ver turmas e ativar/desativar
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
                    System.err.println("Erro ao alterar status do professor: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            HorizontalLayout actions = new HorizontalLayout(editar, verTurmas, toggle);
            return actions;
        }).setHeader("Ações").setAutoWidth(true);

        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(event -> editProfessor(event.getValue()));
    }

    private void openTurmasDialog(Professor professor) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Turmas atribuídas a " + professor.getNomeCompleto());
        dialog.setWidth("900px");
        dialog.setHeight("70vh");
        dialog.setDraggable(true);
        dialog.setResizable(true);

        var rows = professorTurmaService.listTurmasResumoByProfessor(professor.getId());

        if (rows == null || rows.isEmpty()) {
            dialog.add(new H3("Nenhuma turma atribuída."));
        } else {
            Grid<com.gestaoescolar.dto.TurmaResumoDTO> turmasGrid = new Grid<>(com.gestaoescolar.dto.TurmaResumoDTO.class, false);
            turmasGrid.addColumn(com.gestaoescolar.dto.TurmaResumoDTO::getCodigo).setHeader("Código").setAutoWidth(true);
            turmasGrid.addColumn(com.gestaoescolar.dto.TurmaResumoDTO::getNome).setHeader("Nome").setAutoWidth(true);
            turmasGrid.setSizeFull();
            turmasGrid.setItems(rows);
            dialog.add(turmasGrid);
        }
        dialog.open();
    }

    // DTO leve para exibição no diálogo (evita tocar em proxies JPA)
    private record TurmaResumo(String codigo, String nome) {}

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
            // Mostrar mensagem ao usuário e logar para depuração
            String msg = ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido";
            Notification.show("Erro ao salvar: " + msg, 5000, Notification.Position.MIDDLE);
            System.err.println("Erro ao salvar professor: " + msg);
            ex.printStackTrace();
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
        // limpar seleção da grid
        grid.getSelectionModel().deselectAll();
    }

    private void updateList() {
        grid.setItems(professorService.listarTodosProfessores(usuarioLogado));
    }
}