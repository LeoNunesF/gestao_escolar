package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.enums.Serie;
import com.gestaoescolar.model.enums.Turno;
import com.gestaoescolar.service.AnoLetivoService;
import com.gestaoescolar.service.auth.AuthService;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.service.escola.ProfessorTurmaService;
import com.gestaoescolar.service.escola.TurmaService;
import com.gestaoescolar.service.escola.EnrollmentService; // ADICIONE ESTE IMPORT
import com.gestaoescolar.views.components.AssignProfessorDialog;
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
import com.gestaoescolar.service.escola.CurriculumService;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "diretor/turmas", layout = MainLayout.class)
@PageTitle("Gestão de Turmas | Gestão Escolar")
public class TurmasView extends VerticalLayout {

    private final TurmaService turmaService;
    private final AnoLetivoService anoLetivoService;
    private final AuthService authService;
    private final Usuario usuarioLogado;

    private final ProfessorService professorService;
    private final ProfessorTurmaService professorTurmaService;
    private final CurriculumService curriculumService; // ADICIONE ESTE CAMPO

    private final EnrollmentService enrollmentService; // ADICIONE ESTE CAMPO

    private final Grid<Turma> grid = new Grid<>(Turma.class);
    private final TextField filterText = new TextField();
    private final ComboBox<AnoLetivo> filterAnoLetivo = new ComboBox<>();
    private final ComboBox<Serie> filterSerie = new ComboBox<>();
    private final ComboBox<Turno> filterTurno = new ComboBox<>();
    private final ComboBox<Boolean> filterAtiva = new ComboBox<>();

    // AJUSTE O CONSTRUTOR PARA RECEBER enrollmentService
    public TurmasView(TurmaService turmaService,
                      AnoLetivoService anoLetivoService,
                      CurriculumService curriculumService, // <- novo parâmetro
                      AuthService authService,
                      ProfessorService professorService,
                      ProfessorTurmaService professorTurmaService,
                      EnrollmentService enrollmentService) { // <- novo parâmetro
        this.turmaService = turmaService;
        this.anoLetivoService = anoLetivoService;
        this.authService = authService;
        this.usuarioLogado = authService.getUsuarioLogado();
        this.professorService = professorService;
        this.professorTurmaService = professorTurmaService;
        this.enrollmentService = enrollmentService; // atribuição
        this.curriculumService = curriculumService; // atribuição

        setSizeFull();
        setPadding(true);

        add(new H2("Gestão de Turmas"));
        add(createToolbar());
        configureGrid();
        add(grid);

        updateList();
    }

    private HorizontalLayout createToolbar() {
        filterText.setPlaceholder("Filtrar por nome ou código...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        filterAnoLetivo.setPlaceholder("Todos os anos");
        filterAnoLetivo.setItems(anoLetivoService.findAll());
        filterAnoLetivo.setItemLabelGenerator(ano -> "Ano " + ano.getAno());
        filterAnoLetivo.addValueChangeListener(e -> updateList());

        filterSerie.setPlaceholder("Todas as séries");
        filterSerie.setItems(Arrays.asList(Serie.values()));
        filterSerie.setItemLabelGenerator(Serie::getNome);
        filterSerie.addValueChangeListener(e -> updateList());

        filterTurno.setPlaceholder("Todos os turnos");
        filterTurno.setItems(Arrays.asList(Turno.values()));
        filterTurno.setItemLabelGenerator(Turno::getDescricao);
        filterTurno.addValueChangeListener(e -> updateList());

        filterAtiva.setPlaceholder("Todos os status");
        filterAtiva.setItems(true, false);
        filterAtiva.setItemLabelGenerator(ativa -> ativa ? "Ativas" : "Inativas");
        filterAtiva.addValueChangeListener(e -> updateList());

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

        grid.addColumn(Turma::getCodigo).setHeader("Código").setAutoWidth(true).setSortable(true);
        grid.addColumn(Turma::getNomeTurma).setHeader("Nome").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getSerie().getNome()).setHeader("Série").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getNivel().getDescricao()).setHeader("Nível").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getTurno().getDescricao()).setHeader("Turno").setAutoWidth(true).setSortable(true);
        grid.addColumn(turma -> turma.getAnoLetivo().getAno()).setHeader("Ano Letivo").setAutoWidth(true).setSortable(true);

        grid.addColumn(turma ->
                turma.getCapacidade() != null ?
                        turma.getVagasDisponiveis() + "/" + turma.getCapacidade() :
                        "-"
        ).setHeader("Vagas").setAutoWidth(true);

        grid.addColumn(Turma::getSala).setHeader("Sala").setAutoWidth(true);

        grid.addComponentColumn(turma -> {
            Icon statusIcon = turma.isAtiva() ? new Icon(VaadinIcon.CHECK) : new Icon(VaadinIcon.CLOSE);
            statusIcon.setColor(turma.isAtiva() ? "green" : "red");
            return statusIcon;
        }).setHeader("Ativa").setAutoWidth(true);

        // Professores atribuídos (agora exibe o papel para qualquer vínculo)
        grid.addColumn(turma -> formatarProfessoresAtribuidos(turma.getId()))
                .setHeader("Professores")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Ações")
                .setAutoWidth(true);

        grid.getColumns().forEach(col -> col.setResizable(true));
        grid.setSelectionMode(Grid.SelectionMode.NONE);



    }

    private String formatarProfessoresAtribuidos(Long turmaId) {
        List<ProfessorTurma> atribuicoes = professorTurmaService.listByTurma(turmaId);
        if (atribuicoes == null || atribuicoes.isEmpty()) return "Sem professor";
        return atribuicoes.stream()
                .map(pt -> {
                    String nome = (pt.getProfessor() != null && pt.getProfessor().getNomeCompleto() != null)
                            ? pt.getProfessor().getNomeCompleto()
                            : "(sem nome)";
                    String papel = switch (pt.getPapel()) {
                        case TITULAR -> "Titular";
                        case SUBSTITUTO -> "Substituto";
                        case COORDENADOR -> "Coordenador";
                        default -> "";
                    };
                    return papel.isBlank() ? nome : (nome + " (" + papel + ")");
                })
                .collect(Collectors.joining(", "));
    }

    private HorizontalLayout createActionButtons(Turma turma) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addClickListener(e -> openForm(turma));
        editButton.setTooltipText("Editar turma");

        Button statusButton = new Button(turma.isAtiva() ?
                new Icon(VaadinIcon.BAN) : new Icon(VaadinIcon.CHECK));
        statusButton.addClickListener(e -> toggleStatusTurma(turma));
        statusButton.setTooltipText(turma.isAtiva() ? "Desativar turma" : "Ativar turma");

        Button assignButton = new Button(new Icon(VaadinIcon.USER_STAR));
        assignButton.setTooltipText("Atribuir professor");
        assignButton.addClickListener(e -> {
            AssignProfessorDialog dialog = new AssignProfessorDialog(
                    turma, professorService, professorTurmaService, usuarioLogado);
            dialog.open();
            dialog.addDetachListener(dl -> updateList());
        });

        // NOVO BOTÃO: "Alunos Matriculados"
        Button matriculasButton = new Button("Alunos Matriculados", new Icon(VaadinIcon.USERS));
        matriculasButton.addClickListener(e -> {
            TurmaMatriculasDialog d = new TurmaMatriculasDialog(
                    enrollmentService,
                    turmaService,
                    usuarioLogado,
                    turma,
                    this::updateList
            );
            d.open();
        });

        // OFERTAS DISCIPLINAS EM LOTE
        Button ofertarLote = new Button("Ofertar disciplinas (lote)", e -> {
            TurmaOfertaDisciplinasDialog dlg = new TurmaOfertaDisciplinasDialog(
                    curriculumService, // certifique-se de ter este campo injetado na view
                    turma,
                    this::updateList
            );
            dlg.open();
        });
        layout.add(ofertarLote);

        // botão para gerir disciplinas na turma
        Button disciplinasButton = new Button("Disciplinas", e -> {
            TurmaDisciplinaDialog dlg = new TurmaDisciplinaDialog(
                    curriculumService, // injete CurriculumService no construtor da view
                    turmaService,
                    professorService,
                    turma,
                    this::updateList
            );
            dlg.open();
        });
        layout.add(disciplinasButton);

        // Aplicar GRADE
        Button aplicarGrade = new Button("Aplicar grade", e -> {
            TurmaAplicarGradeDialog dlg = new TurmaAplicarGradeDialog(
                    curriculumService, // certifique-se de injetar o serviço na view
                    turma,
                    this::updateList
            );
            dlg.open();
        });
        layout.add(aplicarGrade);

        layout.add(editButton, statusButton, assignButton, matriculasButton);
        return layout;

    }

    private void updateList() {
        List<Turma> turmas = turmaService.listarTodasComAnoLetivo();
        grid.setItems(turmas);
    }

    private void openForm(Turma turma) {
        TurmaForm form = new TurmaForm(turmaService, anoLetivoService, usuarioLogado, turma, this::updateList);
        form.open();
    }

    private void toggleStatusTurma(Turma turma) {
        try {
            turmaService.toggleStatusTurma(turma.getId(), usuarioLogado);
            updateList();
        } catch (Exception e) {
            showError("Erro ao alterar status: " + e.getMessage());
        }
    }


    private void showError(String mensagem) {
        getUI().ifPresent(ui -> ui.getPage().executeJs("alert($0)", mensagem));
    }
    
}