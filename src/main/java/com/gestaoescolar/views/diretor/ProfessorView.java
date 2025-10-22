package com.gestaoescolar.views.diretor;
import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.auth.AuthService;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.persistence.*;

@Route(value = "diretor/professores", layout = MainLayout.class)
@PageTitle("Gestão de Professores")
public class ProfessorView extends VerticalLayout {

    private final ProfessorService professorService;
    private final AuthService authService;

    private Grid<Professor> grid = new Grid<>(Professor.class, false);
    private ProfessorForm form;
    private Usuario usuarioLogado;

    public ProfessorView(ProfessorService professorService, AuthService authService) {
        this.professorService = professorService;
        this.authService = authService;
        this.usuarioLogado = authService.getUsuarioLogado();

        setSizeFull();
        configureGrid();
        configureForm();

        Button novoProfessor = new Button("Novo Professor", e -> form.setProfessor(new Professor()));

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
        professorService.criarProfessor(event.getProfessor(), usuarioLogado);
        updateList();
        closeEditor();
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
}
