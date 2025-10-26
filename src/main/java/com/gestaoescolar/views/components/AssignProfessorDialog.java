package com.gestaoescolar.views.components;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.service.escola.ProfessorTurmaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;

import java.util.List;

/**
 * Diálogo reutilizável para atribuir um Professor a uma Turma.
 * Uso: new AssignProfessorDialog(turma, professorService, professorTurmaService, usuarioLogado)
 */
public class AssignProfessorDialog extends Dialog {

    private final ProfessorService professorService;
    private final ProfessorTurmaService professorTurmaService;
    private final Turma turma;

    private final ComboBox<Professor> professorCombo = new ComboBox<>("Professor");
    private final ComboBox<ProfessorTurma.Papel> papelCombo = new ComboBox<>("Papel");
    private final TextField disciplina = new TextField("Disciplina (opcional)");
    private final DatePicker dataInicio = new DatePicker("Data Início");
    private final DatePicker dataTermino = new DatePicker("Data Término");
    private final Button salvar = new Button("Salvar");
    private final Button cancelar = new Button("Cancelar");

    public AssignProfessorDialog(Turma turma,
                                 ProfessorService professorService,
                                 ProfessorTurmaService professorTurmaService,
                                 Object usuarioLogado) {
        this.turma = turma;
        this.professorService = professorService;
        this.professorTurmaService = professorTurmaService;

        setWidth("520px");
        add(new H3("Atribuir Professor à Turma: " + (turma != null ? turma.getCodigo() : "")));

        configurarCampos();
        createLayout();
        carregarProfessores(usuarioLogado);
    }

    private void configurarCampos() {
        professorCombo.setItemLabelGenerator(p -> p.getNomeCompleto() + " (" + p.getCpf() + ")");
        papelCombo.setItems(ProfessorTurma.Papel.values());
        papelCombo.setValue(ProfessorTurma.Papel.TITULAR);

        salvar.addClickListener(e -> onSalvar());
        cancelar.addClickListener(e -> close());
    }

    private void createLayout() {
        FormLayout form = new FormLayout();
        form.add(professorCombo, papelCombo, disciplina, dataInicio, dataTermino, salvar, cancelar);
        add(form);
    }

    private void carregarProfessores(Object usuarioLogado) {
        // tenta usar método existente do serviço
        List<Professor> lista = professorService.listarTodosProfessores(null); // se seu service exige usuarioLogado, passe-o
        professorCombo.setItems(lista);
    }

    private void onSalvar() {
        Professor selecionado = professorCombo.getValue();
        if (selecionado == null) {
            Notification.show("Selecione um professor", 2500, Notification.Position.MIDDLE);
            return;
        }
        ProfessorTurma.Papel papel = papelCombo.getValue();
        String disc = disciplina.getValue();
        java.time.LocalDate inicio = dataInicio.getValue();
        java.time.LocalDate termino = dataTermino.getValue();

        try {
            professorTurmaService.assignProfessorToTurma(selecionado.getId(), turma.getId(),
                    papel, disc, inicio, termino);
            Notification.show("Professor atribuído com sucesso", 2000, Notification.Position.BOTTOM_START);
            close();
        } catch (Exception ex) {
            Notification.show("Erro ao atribuir: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }
}