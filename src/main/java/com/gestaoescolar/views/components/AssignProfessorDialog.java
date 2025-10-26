package com.gestaoescolar.views.components;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.ProfessorTurma;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.service.escola.ProfessorTurmaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Diálogo para atribuir um Professor a uma Turma.
 * CORREÇÃO: usar o Usuario logado ao consultar o ProfessorService (evita SecurityException).
 */
public class AssignProfessorDialog extends Dialog {

    private final ProfessorService professorService;
    private final ProfessorTurmaService professorTurmaService;
    private final Turma turma;
    private final Usuario usuarioLogado;

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
                                 Usuario usuarioLogado) {
        this.turma = turma;
        this.professorService = professorService;
        this.professorTurmaService = professorTurmaService;
        this.usuarioLogado = usuarioLogado;

        setWidth("520px");
        add(new H3("Atribuir Professor à Turma: " + (turma != null ? turma.getCodigo() : "")));

        configurarCampos();
        configurarDatePickersPtBR();
        createLayout();
        carregarProfessores(); // usa usuarioLogado corretamente
    }

    private void configurarCampos() {
        professorCombo.setItemLabelGenerator(p -> {
            String cpf = p.getCpf() == null ? "" : formatCpf(p.getCpf());
            return p.getNomeCompleto() + (cpf.isBlank() ? "" : " (" + cpf + ")");
        });
        professorCombo.setPlaceholder("Selecione um professor");

        papelCombo.setItems(ProfessorTurma.Papel.values());
        papelCombo.setValue(ProfessorTurma.Papel.TITULAR);

        salvar.addClickListener(e -> onSalvar());
        cancelar.addClickListener(e -> close());
    }

    private void configurarDatePickersPtBR() {
        Locale ptBR = new Locale("pt", "BR");
        dataInicio.setLocale(ptBR);
        dataTermino.setLocale(ptBR);
        dataInicio.setPlaceholder("dd/mm/aaaa");
        dataTermino.setPlaceholder("dd/mm/aaaa");
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setMonthNames(List.of(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        ));
        i18n.setWeekdays(List.of(
                "Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira",
                "Quinta-feira", "Sexta-feira", "Sábado"
        ));
        i18n.setWeekdaysShort(List.of("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"));
        i18n.setToday("Hoje");
        i18n.setCancel("Cancelar");
        i18n.setFirstDayOfWeek(1);
        dataInicio.setI18n(i18n);
        dataTermino.setI18n(i18n);
    }

    private void createLayout() {
        FormLayout form = new FormLayout();
        form.add(professorCombo, papelCombo, disciplina, dataInicio, dataTermino, salvar, cancelar);
        add(form);
    }

    private void carregarProfessores() {
        try {
            if (usuarioLogado == null) {
                Notification.show("Sessão expirada. Faça login novamente.", 3500, Notification.Position.MIDDLE);
                close();
                return;
            }
            List<Professor> lista = professorService.listarTodosProfessores(usuarioLogado);
            professorCombo.setItems(lista);
            if (!lista.isEmpty()) {
                professorCombo.setValue(lista.get(0));
            }
        } catch (SecurityException se) {
            Notification.show("Permissão insuficiente para listar professores.", 4000, Notification.Position.MIDDLE);
            close();
        } catch (Exception ex) {
            Notification.show("Erro ao carregar professores: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            close();
        }
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
            professorTurmaService.assignProfessorToTurma(
                    selecionado.getId(),
                    turma.getId(),
                    papel,
                    disc,
                    inicio,
                    termino
            );
            String periodo = "";
            if (inicio != null) {
                periodo += " a partir de " + inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            if (termino != null) {
                periodo += " até " + termino.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            Notification.show("Professor atribuído com sucesso" + periodo, 2500, Notification.Position.BOTTOM_START);
            close();
        } catch (Exception ex) {
            Notification.show("Erro ao atribuir: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private String formatCpf(String input) {
        String d = input == null ? "" : input.replaceAll("\\D", "");
        if (d.length() == 11) {
            return String.format("%s.%s.%s-%s",
                    d.substring(0, 3),
                    d.substring(3, 6),
                    d.substring(6, 9),
                    d.substring(9, 11));
        }
        return input == null ? "" : input;
    }
}