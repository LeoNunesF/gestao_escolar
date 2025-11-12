package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Disciplina;
import com.gestaoescolar.model.GradeCurricular;
import com.gestaoescolar.model.GradeCurricularItem;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.TurmaDisciplina;
import com.gestaoescolar.service.escola.CurriculumService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.*;
import java.util.stream.Collectors;

public class TurmaAplicarGradeDialog extends Dialog {

    private final CurriculumService curriculumService;
    private final Turma turma;
    private final Runnable onChanged;

    private final ComboBox<GradeCurricular> gradeSelect = new ComboBox<>("Grade Curricular");
    private final Grid<GradeCurricularItem> grid = new Grid<>(GradeCurricularItem.class, false);
    private final IntegerField cargaPadrao = new IntegerField("Carga Horária (aplicar às selecionadas — opcional)");
    private final Span info = new Span();

    private Set<Long> disciplinasJaAtribuidasIds = Set.of();

    public TurmaAplicarGradeDialog(CurriculumService curriculumService,
                                   Turma turma,
                                   Runnable onChanged) {
        this.curriculumService = curriculumService;
        this.turma = turma;
        this.onChanged = onChanged;

        setHeaderTitle("Aplicar grade — " + turma.getCodigo() + " - " + turma.getNomeTurma());
        setWidth("960px");
        setHeight("75vh");
        setDraggable(true);
        setResizable(true);

        configureComponents();
        loadContext();
    }

    // Novo: pré-seleciona a grade ao abrir a partir do atalho
    public void preselectGrade(GradeCurricular g) {
        if (g != null) {
            gradeSelect.setValue(g);
        }
    }

    private void configureComponents() {
        gradeSelect.setItemLabelGenerator(g -> {
            String s = g.getSerie() != null ? " (" + g.getSerie().name() + ")" : "";
            return g.getNome() + s;
        });
        gradeSelect.addValueChangeListener(ev -> loadItens());

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(i -> label(i.getDisciplina())).setHeader("Disciplina").setAutoWidth(true);
        grid.addColumn(i -> i.getCargaHoraria() != null ? i.getCargaHoraria().toString() : "")
                .setHeader("Carga (grade)").setAutoWidth(true);
        grid.addColumn(i -> {
            Disciplina d = i.getDisciplina();
            if (d == null) return "";
            return disciplinasJaAtribuidasIds.contains(d.getId()) ? "Sim" : "Não";
        }).setHeader("Já atribuída?").setAutoWidth(true);

        grid.addSelectionListener(ev -> updateInfo());

        cargaPadrao.setMin(0);
        cargaPadrao.setStepButtonsVisible(true);

        Button aplicar = new Button("Adicionar selecionadas", e -> onAplicar());
        Button fechar = new Button("Fechar", e -> close());

        HorizontalLayout actions = new HorizontalLayout(aplicar, fechar);
        add(gradeSelect, grid, cargaPadrao, info, actions);
    }

    private void loadContext() {
        gradeSelect.setItems(curriculumService.listGrades());

        List<TurmaDisciplina> tds = curriculumService.listByTurma(turma.getId());
        disciplinasJaAtribuidasIds = tds.stream()
                .map(td -> td.getDisciplina() != null ? td.getDisciplina().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        updateInfo();
    }

    private void loadItens() {
        GradeCurricular g = gradeSelect.getValue();
        if (g == null) {
            grid.setItems(List.of());
            updateInfo();
            return;
        }
        List<GradeCurricularItem> itens = curriculumService.listGradeItems(g.getId());
        grid.setItems(itens);
        grid.deselectAll();

        for (GradeCurricularItem it : itens) {
            Disciplina d = it.getDisciplina();
            if (d != null && !disciplinasJaAtribuidasIds.contains(d.getId())) {
                grid.select(it);
            }
        }
        updateInfo();
    }

    private void onAplicar() {
        GradeCurricular g = gradeSelect.getValue();
        if (g == null) {
            Notification.show("Selecione uma grade.", 3000, Notification.Position.MIDDLE);
            return;
        }
        Set<GradeCurricularItem> selecionados = grid.getSelectedItems();
        if (selecionados == null || selecionados.isEmpty()) {
            Notification.show("Selecione ao menos uma disciplina da grade.", 3000, Notification.Position.MIDDLE);
            return;
        }
        Integer carga = cargaPadrao.getValue() != null ? cargaPadrao.getValue() : null;

        int adicionadas = 0;
        int ignoradas = 0;
        List<String> ignoradasRotulos = new ArrayList<>();

        for (GradeCurricularItem it : selecionados) {
            Disciplina d = it.getDisciplina();
            if (d == null) continue;
            if (disciplinasJaAtribuidasIds.contains(d.getId())) {
                ignoradas++;
                ignoradasRotulos.add(label(d));
                continue;
            }
            try {
                Integer cargaUsar = (carga != null ? carga : it.getCargaHoraria());
                curriculumService.addDisciplinaToTurma(turma.getId(), d.getId(), cargaUsar);
                adicionadas++;
                disciplinasJaAtribuidasIds.add(d.getId());
            } catch (Exception ex) {
                ignoradas++;
                ignoradasRotulos.add(label(d));
            }
        }

        Notification.show("Adicionadas: " + adicionadas + ". Ignoradas: " + ignoradas +
                        (ignoradasRotulos.isEmpty() ? "" : " (" + String.join(", ", limit(ignoradasRotulos, 5)) + (ignoradasRotulos.size() > 5 ? ", ..." : "") + ")"),
                4000, Notification.Position.BOTTOM_START);

        if (adicionadas > 0 && onChanged != null) onChanged.run();

        loadItens();
    }

    private List<String> limit(List<String> lista, int max) {
        if (lista.size() <= max) return lista;
        return lista.subList(0, max);
    }

    private void updateInfo() {
        Set<GradeCurricularItem> sel = grid.getSelectedItems();
        long jaAtrib = sel.stream()
                .map(GradeCurricularItem::getDisciplina)
                .filter(Objects::nonNull)
                .filter(d -> disciplinasJaAtribuidasIds.contains(d.getId()))
                .count();
        info.setText("Selecionadas: " + (sel != null ? sel.size() : 0) + " | Já atribuídas na turma: " + jaAtrib + " (serão ignoradas).");
    }

    private String label(Disciplina d) {
        String c = d.getCodigo() != null ? d.getCodigo() : "";
        String n = d.getNome() != null ? d.getNome() : "";
        return c + (n.isBlank() ? "" : " - " + n);
    }
}