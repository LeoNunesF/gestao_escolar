package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.auth.AuthService;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.service.escola.ProfessorTurmaService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "diretor/professores", layout = MainLayout.class)
@PageTitle("Gestão de Professores")
public class ProfessorView extends VerticalLayout {

    private final ProfessorService professorService;
    private final ProfessorTurmaService professorTurmaService;
    private final AuthService authService;

    private Grid<Professor> grid = new Grid<>(Professor.class, false);
    private ProfessorForm form;
    private Usuario usuarioLogado;

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

        Button novoProfessor = new Button("Novo Professor", e -> {
            form.setProfessor(new Professor());
            form.setVisible(true);
        });

        HorizontalLayout toolbar = new HorizontalLayout(novoProfessor);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);

        add(toolbar, grid, form);
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addColumn(Professor::getNomeCompleto).setHeader("Nome");
        grid.addColumn(Professor::getCpf).setHeader("CPF");
        grid.addColumn(Professor::getEmail).setHeader("Email");
        grid.addColumn(Professor::getTelefone).setHeader("Telefone");
        grid.addColumn(Professor::getFormacao).setHeader("Formação");
        grid.addColumn(prof -> prof.isAtivo() ? "Sim" : "Não").setHeader("Ativo");

        // Ações: editar, ativar/desativar e ver turmas
        grid.addComponentColumn(prof -> {
            Button editar = new Button("Editar", ev -> editProfessor(prof));
            Button toggle = new Button(prof.isAtivo() ? "Desativar" : "Reativar", ev -> {
                try {
                    if (prof.isAtivo()) {
                        professorService.desativarProfessor(prof.getId(), usuarioLogado);
                    } else {
                        professorService.reativarProfessor(prof.getId(), usuarioLogado);
                    }
                    updateList();
                } catch (Exception ex) {
                    Notification.show("Erro: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });
            Button verTurmas = new Button("Ver Turmas", ev -> showTurmasDialog(prof));
            HorizontalLayout actions = new HorizontalLayout(editar, toggle, verTurmas);
            return actions;
        }).setHeader("Ações");

        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(event -> editProfessor(event.getValue()));
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
                Notification.show("Professor criado com sucesso.", 2000, Notification.Position.BOTTOM_START);
            } else {
                professorService.atualizarProfessor(p.getId(), p, usuarioLogado);
                Notification.show("Professor atualizado com sucesso.", 2000, Notification.Position.BOTTOM_START);
            }
            updateList();
            closeEditor();
        } catch (Exception ex) {
            Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
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
    }

    private void updateList() {
        grid.setItems(professorService.listarTodosProfessores(usuarioLogado));
    }

    private void showTurmasDialog(Professor professor) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Turmas de " + professor.getNomeFormatado());
        dialog.setModal(true);
        dialog.setWidth("600px");

        // Buscar turmas atribuídas ao professor
        List<ProfessorTurma> professorTurmas = professorTurmaService.listByProfessor(professor.getId());

        // Criar grid para exibir as turmas
        Grid<ProfessorTurma> turmasGrid = new Grid<>(ProfessorTurma.class, false);
        turmasGrid.addColumn(pt -> pt.getTurma().getCodigo()).setHeader("Código").setAutoWidth(true);
        turmasGrid.addColumn(pt -> pt.getTurma().getNomeTurma()).setHeader("Nome").setAutoWidth(true);
        turmasGrid.addColumn(ProfessorTurma::getDisciplina).setHeader("Disciplina").setAutoWidth(true);
        turmasGrid.addColumn(ProfessorTurma::getPapel).setHeader("Papel").setAutoWidth(true);
        turmasGrid.setItems(professorTurmas);
        turmasGrid.setHeight("300px");

        Button closeButton = new Button("Fechar", e -> dialog.close());
        
        VerticalLayout layout = new VerticalLayout(turmasGrid, closeButton);
        layout.setAlignItems(Alignment.END);
        
        dialog.add(layout);
        dialog.open();
    }
}