package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.*;
import com.gestaoescolar.service.escola.CurriculumService;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.service.escola.TurmaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.List;

public class TurmaDisciplinaDialog extends Dialog {

    private final CurriculumService curriculumService;
    private final TurmaService turmaService;
    private final ProfessorService professorService;
    private final Turma turma;
    private final Runnable onChanged;

    private final Grid<TurmaDisciplina> grid = new Grid<>(TurmaDisciplina.class, false);

    private final ComboBox<Disciplina> disciplinaSelect = new ComboBox<>("Disciplina");
    private final IntegerField cargaField = new IntegerField("Carga Horária (opcional)");

    public TurmaDisciplinaDialog(CurriculumService curriculumService,
                                 TurmaService turmaService,
                                 ProfessorService professorService,
                                 Turma turma,
                                 Runnable onChanged) {
        this.curriculumService = curriculumService;
        this.turmaService = turmaService;
        this.professorService = professorService;
        this.turma = turma;
        this.onChanged = onChanged;

        setHeaderTitle("Disciplinas — " + turma.getCodigo() + " - " + turma.getNomeTurma());
        setWidth("900px");
        setHeight("70vh");
        setDraggable(true);
        setResizable(true);

        configureGrid();
        configureForm();

        refresh();
    }

    private void configureGrid() {
        grid.addColumn(td -> td.getDisciplina() != null ? td.getDisciplina().getCodigo() + " - " + td.getDisciplina().getNome() : "")
                .setHeader("Disciplina").setAutoWidth(true);
        grid.addColumn(td -> td.getCargaHoraria() != null ? td.getCargaHoraria().toString() : "")
                .setHeader("Carga").setAutoWidth(true);

        grid.addComponentColumn(td -> {
            Button remover = new Button("Remover", e -> {
                try {
                    curriculumService.removeDisciplinaFromTurma(td.getId());
                    refresh();
                    if (onChanged != null) onChanged.run();
                } catch (Exception ex) {
                    Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });
            return remover;
        }).setHeader("Ações");
    }

    private void configureForm() {
        List<Disciplina> all = curriculumService.listAllDisciplines();
        disciplinaSelect.setItems(all);
        disciplinaSelect.setItemLabelGenerator(d -> d.getCodigo() + " - " + d.getNome());

        FormLayout form = new FormLayout();
        form.add(disciplinaSelect, cargaField);

        Button adicionar = new Button("Adicionar", e -> {
            Disciplina sel = disciplinaSelect.getValue();
            if (sel == null) {
                Notification.show("Selecione uma disciplina", 2500, Notification.Position.MIDDLE);
                return;
            }
            try {
                curriculumService.addDisciplinaToTurma(turma.getId(), sel.getId(), cargaField.getValue());
                refresh();
                if (onChanged != null) onChanged.run();
            } catch (Exception ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        Button fechar = new Button("Fechar", e -> close());

        HorizontalLayout actions = new HorizontalLayout(adicionar, fechar);
        form.add(actions);

        add(form, grid);
    }

    private void refresh() {
        List<TurmaDisciplina> list = curriculumService.listByTurma(turma.getId());
        grid.setItems(list);
    }
}