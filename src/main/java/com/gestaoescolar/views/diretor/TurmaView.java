package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.auth.AuthService;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.service.escola.ProfessorTurmaService;
import com.gestaoescolar.service.escola.TurmaService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "diretor/turma", layout = MainLayout.class)
@PageTitle("Gestão de Turmas | Gestão Escolar")
public class TurmaView extends VerticalLayout {

    private final TurmaService turmaService;
    private final ProfessorService professorService;
    private final ProfessorTurmaService professorTurmaService;
    private final AuthService authService;
    private final Usuario usuarioLogado;

    private final Grid<Turma> grid = new Grid<>(Turma.class, false);
    private final TextField filterText = new TextField();
    private final Button assignProfessorButton = new Button("Atribuir Professor");
    private final Button novaTurmaButton = new Button("Nova Turma");

    private Turma selectedTurma;

    public TurmaView(TurmaService turmaService,
                     ProfessorService professorService,
                     ProfessorTurmaService professorTurmaService,
                     AuthService authService) {
        this.turmaService = turmaService;
        this.professorService = professorService;
        this.professorTurmaService = professorTurmaService;
        this.authService = authService;
        this.usuarioLogado = authService.getUsuarioLogado();

        setSizeFull();
        setPadding(true);

        add(new H2("Gestão de Turmas"));
        add(createToolbar());
        configureGrid();
        add(grid);

        updateList();
    }

    private HorizontalLayout createToolbar() {
        // Filtro por nome/código
        filterText.setPlaceholder("Filtrar por nome ou código...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        // Botão Atribuir Professor (habilitado apenas se uma turma estiver selecionada)
        assignProfessorButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        assignProfessorButton.setEnabled(false);
        assignProfessorButton.addClickListener(e -> openAssignProfessorDialog());

        // Botão Nova Turma (mantém comportamento atual - abre diálogo/formulário)
        novaTurmaButton.addClickListener(e -> openNovaTurmaForm());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, assignProfessorButton, novaTurmaButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(Alignment.END);

        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Colunas: codigo, nome, serie
        grid.addColumn(Turma::getCodigo).setHeader("Código").setAutoWidth(true).setSortable(true);
        grid.addColumn(Turma::getNomeTurma).setHeader("Nome").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getSerie().getNome()).setHeader("Série").setAutoWidth(true).setSortable(true);

        // Modo de seleção único
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedTurma = event.getValue();
            assignProfessorButton.setEnabled(selectedTurma != null);
        });
    }

    private void updateList() {
        String filterValue = filterText.getValue();
        if (filterValue == null || filterValue.trim().isEmpty()) {
            grid.setItems(turmaService.listarTodasComAnoLetivo());
        } else {
            // Filtrar por código ou nome
            grid.setItems(turmaService.listarTodasComAnoLetivo().stream()
                    .filter(t -> t.getCodigo().toLowerCase().contains(filterValue.toLowerCase()) ||
                                 t.getNomeTurma().toLowerCase().contains(filterValue.toLowerCase()))
                    .toList());
        }
    }

    private void openAssignProfessorDialog() {
        if (selectedTurma != null) {
            AssignProfessorDialog dialog = new AssignProfessorDialog(
                    selectedTurma,
                    professorService,
                    professorTurmaService,
                    usuarioLogado
            );

            // Adicionar listener para atualizar a lista quando o diálogo for fechado
            dialog.addDetachListener(e -> updateList());
            dialog.open();
        }
    }

    private void openNovaTurmaForm() {
        // Mantém comportamento atual: abre TurmaForm
        // Implementação depende da estrutura existente
        // Por exemplo, pode abrir um diálogo ou navegar para outra view
        // Aqui vamos apenas adicionar um placeholder
        getUI().ifPresent(ui -> ui.navigate("diretor/turmas"));
    }
}
