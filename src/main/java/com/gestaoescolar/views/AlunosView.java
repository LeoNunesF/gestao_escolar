package com.gestaoescolar.views;

import com.gestaoescolar.model.Aluno;
import com.gestaoescolar.service.AlunoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "alunos", layout = MainView.class)
@PageTitle("Alunos | Gestão Escolar")
public class AlunosView extends VerticalLayout {

    private final AlunoService alunoService;
    private final Grid<Aluno> grid = new Grid<>(Aluno.class);
    private final TextField filterText = new TextField();

    public AlunosView(AlunoService alunoService) {
        this.alunoService = alunoService;

        setSizeFull();
        configureGrid();

        add(
                new H2("Gestão de Alunos"),
                getToolbar(),
                grid
        );

        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.removeAllColumns();

        grid.addColumn(Aluno::getMatricula).setHeader("Matrícula").setAutoWidth(true);
        grid.addColumn(Aluno::getNome).setHeader("Nome").setAutoWidth(true);
        grid.addColumn(Aluno::getTurma).setHeader("Turma").setAutoWidth(true);
        grid.addColumn(Aluno::getResponsavel).setHeader("Responsável").setAutoWidth(true);
        grid.addColumn(Aluno::getTelefone).setHeader("Telefone").setAutoWidth(true);
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filtrar por nome...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Adicionar Aluno");
        addButton.addClickListener(e -> adicionarAluno());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return toolbar;
    }

    private void updateList() {
        grid.setItems(alunoService.findByNome(filterText.getValue()));
    }

    private void adicionarAluno() {
        getUI().ifPresent(ui -> ui.navigate(CadastroAlunoView.class));
    }
}