package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.escola.EnrollmentService;
import com.gestaoescolar.service.escola.TurmaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class MatricularAlunoDialog extends Dialog {

    private final EnrollmentService enrollmentService;
    private final TurmaService turmaService;
    private final Usuario usuario;
    private final Long alunoId;
    private final Runnable onSaved;

    private final Select<Turma> turmaSelect = new Select<>();
    private final DatePicker dataInicio = new DatePicker("Data de início");

    private final Button salvar = new Button("Matricular");
    private final Button cancelar = new Button("Cancelar");

    public MatricularAlunoDialog(EnrollmentService enrollmentService,
                                 TurmaService turmaService,
                                 Usuario usuario,
                                 Long alunoId,
                                 Runnable onSaved) {
        this.enrollmentService = enrollmentService;
        this.turmaService = turmaService;
        this.usuario = usuario;
        this.alunoId = alunoId;
        this.onSaved = onSaved;

        setHeaderTitle("Matricular Aluno");
        setWidth("640px");
        setDraggable(true);
        setResizable(true);

        // Turmas (pode filtrar só ativas; use o método disponível no seu TurmaService)
        List<Turma> turmas = turmaService.listarTodasComAnoLetivo(); // ajuste se houver método específico
        turmaSelect.setLabel("Turma");
        turmaSelect.setItems(turmas);
        turmaSelect.setItemLabelGenerator(t -> t.getCodigo() + " - " + t.getNomeTurma());

        configurarDatePickerPtBR(dataInicio);
        dataInicio.setValue(LocalDate.now());

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2)
        );

        form.add(turmaSelect, dataInicio);
        HorizontalLayout actions = new HorizontalLayout(salvar, cancelar);
        form.add(actions);
        form.setColspan(actions, 2);

        salvar.addClickListener(e -> onSave());
        cancelar.addClickListener(e -> close());

        add(form);
    }

    private void onSave() {
        try {
            Turma turma = turmaSelect.getValue();
            if (turma == null) {
                Notification.show("Selecione uma turma.", 3000, Notification.Position.MIDDLE);
                return;
            }
            LocalDate inicio = dataInicio.getValue();
            enrollmentService.enrollStudent(alunoId, turma.getId(), inicio, usuario);
            Notification.show("Matrícula criada.", 2500, Notification.Position.BOTTOM_START);
            close();
            if (onSaved != null) onSaved.run();
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Erro ao matricular";
            Notification.show(msg, 4000, Notification.Position.MIDDLE);
        }
    }

    private void configurarDatePickerPtBR(DatePicker picker) {
        Locale ptBR = new Locale("pt", "BR");
        picker.setLocale(ptBR);
        picker.setPlaceholder("dd/MM/aaaa");

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
        picker.setI18n(i18n);
    }
}