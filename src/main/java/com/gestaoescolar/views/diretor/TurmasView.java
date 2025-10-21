package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.NivelEscolar;
import com.gestaoescolar.model.enums.Serie;
import com.gestaoescolar.model.enums.Turno;
import com.gestaoescolar.service.auth.AuthService;
import com.gestaoescolar.service.escola.TurmaService;
import com.gestaoescolar.service.AnoLetivoService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

@Route(value = "diretor/turmas", layout = MainLayout.class)
@PageTitle("Gestão de Turmas | Gestão Escolar")
public class TurmasView extends VerticalLayout {

    private final TurmaService turmaService;
    private final AnoLetivoService anoLetivoService;
    private final AuthService authService;
    private final Usuario usuarioLogado;


    private final Grid<Turma> grid = new Grid<>(Turma.class);
    private final TextField filterText = new TextField();
    private final ComboBox<AnoLetivo> filterAnoLetivo = new ComboBox<>();
    private final ComboBox<Serie> filterSerie = new ComboBox<>();
    private final ComboBox<Turno> filterTurno = new ComboBox<>();
    private final ComboBox<Boolean> filterAtiva = new ComboBox<>();

    public TurmasView(TurmaService turmaService, AnoLetivoService anoLetivoService, AuthService authService) {
        this.turmaService = turmaService;
        this.anoLetivoService = anoLetivoService;
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

        // Filtro por ano letivo
        filterAnoLetivo.setPlaceholder("Todos os anos");
        filterAnoLetivo.setItems(anoLetivoService.findAll());
        filterAnoLetivo.setItemLabelGenerator(ano -> "Ano " + ano.getAno());
        filterAnoLetivo.addValueChangeListener(e -> updateList());

        // Filtro por série
        filterSerie.setPlaceholder("Todas as séries");
        filterSerie.setItems(Arrays.asList(Serie.values()));
        filterSerie.setItemLabelGenerator(Serie::getNome);
        filterSerie.addValueChangeListener(e -> updateList());

        // Filtro por turno
        filterTurno.setPlaceholder("Todos os turnos");
        filterTurno.setItems(Arrays.asList(Turno.values()));
        filterTurno.setItemLabelGenerator(Turno::getDescricao);
        filterTurno.addValueChangeListener(e -> updateList());

        // Filtro por status
        filterAtiva.setPlaceholder("Todos os status");
        filterAtiva.setItems(true, false);
        filterAtiva.setItemLabelGenerator(ativa -> ativa ? "Ativas" : "Inativas");
        filterAtiva.addValueChangeListener(e -> updateList());

        // Botão nova turma
        Button addButton = new Button("Nova Turma", new Icon(VaadinIcon.PLUS));
        addButton.addClickListener(e -> openForm(new Turma()));

        HorizontalLayout filters = new HorizontalLayout(filterText, filterAnoLetivo, filterSerie, filterTurno, filterAtiva);
        filters.setAlignItems(Alignment.END);
        filters.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout(filters, addButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(Alignment.END);

        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.removeAllColumns();

        // Colunas principais
        grid.addColumn(Turma::getCodigo).setHeader("Código").setAutoWidth(true).setSortable(true);
        grid.addColumn(Turma::getNomeTurma).setHeader("Nome").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getSerie().getNome()).setHeader("Série").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getNivel().getDescricao()).setHeader("Nível").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getTurno().getDescricao()).setHeader("Turno").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getAnoLetivo().getAno()).setHeader("Ano Letivo").setAutoWidth(true).setSortable(true);

        // Coluna de capacidade e vagas
        grid.addColumn(turma ->
                turma.getCapacidade() != null ?
                        turma.getVagasDisponiveis() + "/" + turma.getCapacidade() :
                        "-"
        ).setHeader("Vagas").setAutoWidth(true);

        // Coluna de sala
        grid.addColumn(Turma::getSala).setHeader("Sala").setAutoWidth(true);

        // Coluna de status com ícone
        grid.addComponentColumn(turma -> {
            Icon statusIcon = turma.isAtiva() ?
                    new Icon(VaadinIcon.CHECK) : new Icon(VaadinIcon.CLOSE);
            statusIcon.setColor(turma.isAtiva() ? "green" : "red");
            return statusIcon;
        }).setHeader("Ativa").setAutoWidth(true);

        // Coluna de professor titular
        grid.addColumn(turma ->
                turma.getProfessorTitular() != null ?
                        turma.getProfessorTitular().getNomeFormatado() :
                        "Sem professor"
        ).setHeader("Professor").setAutoWidth(true);

        // Coluna de ações
        grid.addComponentColumn(turma -> createActionButtons(turma))
                .setHeader("Ações")
                .setAutoWidth(true);

        // Configurações da grid
        grid.getColumns().forEach(col -> col.setResizable(true));
        grid.setSelectionMode(Grid.SelectionMode.NONE);
    }

    private HorizontalLayout createActionButtons(Turma turma) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        // Botão editar
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addClickListener(e -> openForm(turma));
        editButton.setTooltipText("Editar turma");

        // Botão ativar/desativar
        Button statusButton = new Button(turma.isAtiva() ?
                new Icon(VaadinIcon.BAN) : new Icon(VaadinIcon.CHECK));
        statusButton.addClickListener(e -> toggleStatusTurma(turma));
        statusButton.setTooltipText(turma.isAtiva() ? "Desativar turma" : "Ativar turma");

        layout.add(editButton, statusButton);

        return layout;
    }

    private void updateList() {
        // Por enquanto, mostra todas as turmas
        // Futuro: implementar filtros no service
        List<Turma> turmas = turmaService.listarTodasComAnoLetivo();
        grid.setItems(turmas); // injeta o ano letivo
    }

    private void openForm(Turma turma) {
        TurmaForm form = new TurmaForm(turmaService, anoLetivoService, usuarioLogado, turma, this::updateList);
        form.open();
    }

    private void toggleStatusTurma(Turma turma) {
        try {
            turmaService.toggleStatusTurma(turma.getId(), usuarioLogado); // Usuario será injetado depois
            updateList();
        } catch (Exception e) {
            showError("Erro ao alterar status: " + e.getMessage());
        }
    }

    private void showError(String mensagem) {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "alert($0)", mensagem
        ));
    }
}