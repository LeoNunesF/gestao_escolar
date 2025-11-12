package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Disciplina;
import com.gestaoescolar.model.GradeCurricular;
import com.gestaoescolar.model.GradeCurricularItem;
import com.gestaoescolar.model.enums.Serie;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.service.escola.CurriculumService;
import com.gestaoescolar.service.escola.TurmaService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import com.vaadin.flow.component.splitlayout.SplitLayout; // CORRETO: SplitLayout é deste pacote
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "diretor/grades", layout = MainLayout.class)
@PageTitle("Grades Curriculares | Gestão Escolar")
public class GradeCurricularView extends VerticalLayout {

    private final CurriculumService curriculumService;
    private final TurmaService turmaService;

    private final Grid<GradeCurricular> gridGrades = new Grid<>(GradeCurricular.class, false);
    private final Grid<GradeCurricularItem> gridItens = new Grid<>(GradeCurricularItem.class, false);

    private final TextField nome = new TextField("Nome da grade");
    private final ComboBox<Serie> serie = new ComboBox<>("Série (opcional)");

    private final ComboBox<Disciplina> disciplinaAdd = new ComboBox<>("Disciplina");
    private final IntegerField cargaAdd = new IntegerField("Carga Horária (opcional)");

    private GradeCurricular editing;

    public GradeCurricularView(CurriculumService curriculumService,
                               TurmaService turmaService) {
        this.curriculumService = curriculumService;
        this.turmaService = turmaService;

        setSizeFull();
        setPadding(true);

        add(new H2("Grades Curriculares"));

        SplitLayout split = new SplitLayout();
        split.setSizeFull();

        // Lado esquerdo: lista de grades + form
        VerticalLayout left = new VerticalLayout();
        left.setSizeFull();
        left.setPadding(false);
        left.add(createFormGrade(), gridGrades);

        // Lado direito: itens da grade selecionada
        VerticalLayout right = new VerticalLayout();
        right.setSizeFull();
        right.setPadding(false);
        right.add(createFormItem(), gridItens);

        split.addToPrimary(left);
        split.addToSecondary(right);

        add(split);

        configureGrids();
        updateGrades();
    }

    private FormLayout createFormGrade() {
        FormLayout form = new FormLayout();

        serie.setItems(Serie.values());
        serie.setClearButtonVisible(true);

        Button salvar = new Button("Salvar grade", e -> onSaveGrade());
        Button novo = new Button("Nova grade", e -> {
            editing = new GradeCurricular();
            clearFormGrade();
            gridItens.setItems(List.of());
        });
        Button excluir = new Button("Excluir grade", e -> {
            if (editing != null && editing.getId() != null) {
                try {
                    curriculumService.deleteGrade(editing.getId());
                    Notification.show("Grade excluída.", 2500, Notification.Position.BOTTOM_START);
                    editing = null;
                    clearFormGrade();
                    updateGrades();
                    gridItens.setItems(List.of());
                } catch (Exception ex) {
                    Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
                }
            }
        });

        Button aplicarEmTurma = new Button("Aplicar em turma…", e -> openAplicarEmTurmaAtalho());

        // IMPORTANTE: um único container de ações para evitar “Variable 'actions' is already defined”
        HorizontalLayout actions = new HorizontalLayout(salvar, novo, excluir, aplicarEmTurma);

        form.add(nome, serie, actions);
        return form;
    }

    private FormLayout createFormItem() {
        FormLayout form = new FormLayout();

        disciplinaAdd.setItemLabelGenerator(d -> d.getCodigo() + " - " + d.getNome());
        disciplinaAdd.setItems(curriculumService.listAllDisciplines());
        cargaAdd.setMin(0);
        cargaAdd.setStepButtonsVisible(true);

        Button add = new Button("Adicionar à grade", e -> onAddItem());
        form.add(disciplinaAdd, cargaAdd, add);
        return form;
    }

    private void configureGrids() {
        gridGrades.addColumn(GradeCurricular::getNome).setHeader("Nome").setAutoWidth(true);
        gridGrades.addColumn(g -> g.getSerie() != null ? g.getSerie().name() : "").setHeader("Série").setAutoWidth(true);
        gridGrades.asSingleSelect().addValueChangeListener(ev -> editGrade(ev.getValue()));

        gridItens.addColumn(i -> i.getDisciplina() != null ? i.getDisciplina().getCodigo() + " - " + i.getDisciplina().getNome() : "")
                .setHeader("Disciplina").setAutoWidth(true);
        gridItens.addColumn(i -> i.getCargaHoraria() != null ? i.getCargaHoraria().toString() : "")
                .setHeader("Carga").setAutoWidth(true);
        gridItens.addComponentColumn(i -> {
            Button remover = new Button("Remover", e -> {
                try {
                    curriculumService.removeItemFromGrade(i.getId());
                    Notification.show("Disciplina removida da grade.", 2000, Notification.Position.BOTTOM_START);
                    updateItens();
                } catch (Exception ex) {
                    Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
                }
            });
            return remover;
        }).setHeader("Ações");
    }

    private void editGrade(GradeCurricular g) {
        this.editing = g;
        if (g == null) {
            clearFormGrade();
            gridItens.setItems(List.of());
            return;
        }
        nome.setValue(g.getNome() != null ? g.getNome() : "");
        serie.setValue(g.getSerie());
        updateItens();
    }

    private void clearFormGrade() {
        nome.clear();
        serie.clear();
    }

    private void onSaveGrade() {
        try {
            if (editing == null) editing = new GradeCurricular();
            editing.setNome(nome.getValue());
            editing.setSerie(serie.getValue());
            editing = curriculumService.createOrUpdateGrade(editing);
            Notification.show("Grade salva.", 2000, Notification.Position.BOTTOM_START);
            updateGrades();
        } catch (Exception ex) {
            Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void onAddItem() {
        if (editing == null || editing.getId() == null) {
            Notification.show("Salve a grade antes de adicionar disciplinas.", 3000, Notification.Position.MIDDLE);
            return;
        }
        Disciplina d = disciplinaAdd.getValue();
        if (d == null) {
            Notification.show("Selecione uma disciplina.", 2500, Notification.Position.MIDDLE);
            return;
        }
        try {
            curriculumService.addItemToGrade(editing.getId(), d.getId(), cargaAdd.getValue());
            Notification.show("Disciplina adicionada.", 2000, Notification.Position.BOTTOM_START);
            updateItens();
        } catch (Exception ex) {
            Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void updateGrades() {
        gridGrades.setItems(curriculumService.listGrades());
    }

    private void updateItens() {
        if (editing != null && editing.getId() != null) {
            gridItens.setItems(curriculumService.listGradeItems(editing.getId()));
        } else {
            gridItens.setItems(List.of());
        }
    }

    private String msg(Exception ex) {
        return ex.getMessage() != null ? ex.getMessage() : "Erro inesperado";
    }

    private void openAplicarEmTurmaAtalho() {
        if (editing == null || editing.getId() == null) {
            Notification.show("Selecione e salve uma grade antes de aplicar.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Dialog d = new Dialog();
        d.setHeaderTitle("Selecionar turma");

        ComboBox<Turma> cbTurma = new ComboBox<>("Turma");
        cbTurma.setItems(turmaService.listarTodasComAnoLetivo());
        cbTurma.setItemLabelGenerator(t -> t.getCodigo() + " - " + t.getNomeTurma());
        cbTurma.setWidthFull();

        Button continuar = new Button("Continuar", ev -> {
            Turma turma = cbTurma.getValue();
            if (turma == null) {
                Notification.show("Selecione uma turma.", 2500, Notification.Position.MIDDLE);
                return;
            }
            d.close();

            TurmaAplicarGradeDialog dlg = new TurmaAplicarGradeDialog(
                    curriculumService,
                    turma,
                    this::updateItens
            );
            dlg.preselectGrade(editing);
            dlg.open();
        });

        Button fechar = new Button("Fechar", ev -> d.close());

        HorizontalLayout footer = new HorizontalLayout(continuar, fechar);
        d.add(cbTurma);
        d.getFooter().add(footer);
        d.setWidth("520px");
        d.open();
    }
}