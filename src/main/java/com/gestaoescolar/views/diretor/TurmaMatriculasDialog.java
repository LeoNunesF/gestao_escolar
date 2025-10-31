package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Matricula;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.MatriculaStatus;
import com.gestaoescolar.service.escola.EnrollmentService;
import com.gestaoescolar.service.escola.TurmaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TurmaMatriculasDialog extends Dialog {

    private final EnrollmentService enrollmentService;
    private final TurmaService turmaService;
    private final Usuario usuario;
    private final Turma turma;
    private final Runnable onChanged;

    private final Grid<Matricula> grid = new Grid<>(Matricula.class, false);
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TurmaMatriculasDialog(EnrollmentService enrollmentService,
                                 TurmaService turmaService,
                                 Usuario usuario,
                                 Turma turma,
                                 Runnable onChanged) {
        this.enrollmentService = enrollmentService;
        this.turmaService = turmaService;
        this.usuario = usuario;
        this.turma = turma;
        this.onChanged = onChanged;

        setHeaderTitle("Alunos Matriculados — " + turma.getCodigo() + " - " + turma.getNomeTurma());
        setWidth("960px");
        setHeight("70vh");
        setDraggable(true);
        setResizable(true);

        configureGrid();
        refresh();

        Button fechar = new Button("Fechar", e -> close());
        getFooter().add(fechar);

        add(grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(m -> m.getAluno() != null ? m.getAluno().getNomeCompleto() : "(aluno)")
                .setHeader("Aluno").setAutoWidth(true);
        grid.addColumn(m -> mapStatus(m.getStatus())).setHeader("Status").setAutoWidth(true);
        grid.addColumn(m -> m.getDataInicio() != null ? df.format(m.getDataInicio()) : "")
                .setHeader("Início").setAutoWidth(true);
        grid.addColumn(m -> m.getDataTermino() != null ? df.format(m.getDataTermino()) : "")
                .setHeader("Término").setAutoWidth(true);

        grid.addComponentColumn(m -> {
            Button concluir = new Button("Concluir", e -> openConcluirDialog(m));
            Button cancelar = new Button("Cancelar", e -> openCancelarDialog(m));
            Button transferir = new Button("Transferir", e -> openTransferirDialog(m));
            // Só habilitar ações quando ATIVA
            boolean ativa = m.getStatus() == MatriculaStatus.ATIVA;
            concluir.setEnabled(ativa);
            cancelar.setEnabled(ativa);
            transferir.setEnabled(ativa);
            return new HorizontalLayout(concluir, cancelar, transferir);
        }).setHeader("Ações").setAutoWidth(true);
    }

    private void refresh() {
        try {
            List<Matricula> list = enrollmentService.listEnrollmentsByClass(turma.getId(), usuario);
            grid.setItems(list);
        } catch (Exception ex) {
            Notification.show("Erro ao carregar matrículas: " + msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void openConcluirDialog(Matricula m) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Concluir matrícula");
        DatePicker data = new DatePicker("Data de conclusão");
        configurarDatePickerPtBR(data);
        data.setValue(LocalDate.now());

        FormLayout form = new FormLayout();
        form.add(new Span("Aluno: " + (m.getAluno() != null ? m.getAluno().getNomeCompleto() : "")));
        form.add(data);

        Button salvar = new Button("Concluir", e -> {
            try {
                enrollmentService.concludeEnrollment(m.getId(), data.getValue(), usuario);
                Notification.show("Matrícula concluída.", 2500, Notification.Position.BOTTOM_START);
                d.close();
                refresh();
                if (onChanged != null) onChanged.run();
            } catch (Exception ex) {
                Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
            }
        });
        Button cancelar = new Button("Fechar", e -> d.close());
        d.getFooter().add(new HorizontalLayout(salvar, cancelar));

        d.add(form);
        d.open();
    }

    private void openCancelarDialog(Matricula m) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Cancelar matrícula");

        DatePicker data = new DatePicker("Data de cancelamento");
        configurarDatePickerPtBR(data);
        data.setValue(LocalDate.now());

        TextArea motivo = new TextArea("Motivo (opcional)");
        motivo.setWidthFull();
        motivo.setMaxLength(1000);

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.add(new Span("Aluno: " + (m.getAluno() != null ? m.getAluno().getNomeCompleto() : "")));
        form.add(data, motivo);
        form.setColspan(motivo, 2);

        Button salvar = new Button("Cancelar matrícula", e -> {
            try {
                enrollmentService.cancelEnrollment(m.getId(), data.getValue(), motivo.getValue(), usuario);
                Notification.show("Matrícula cancelada.", 2500, Notification.Position.BOTTOM_START);
                d.close();
                refresh();
                if (onChanged != null) onChanged.run();
            } catch (Exception ex) {
                Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
            }
        });
        Button fechar = new Button("Fechar", e -> d.close());
        d.getFooter().add(new HorizontalLayout(salvar, fechar));

        d.add(form);
        d.open();
    }

    private void openTransferirDialog(Matricula m) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Transferir matrícula");

        Select<Turma> turmaSelect = new Select<>();
        turmaSelect.setLabel("Nova turma");
        List<Turma> turmas = turmaService.listarTodasComAnoLetivo(); // pode filtrar por ano, se desejar
        turmaSelect.setItems(turmas);
        turmaSelect.setItemLabelGenerator(t -> t.getCodigo() + " - " + t.getNomeTurma());
        turmaSelect.setValue(null);

        DatePicker data = new DatePicker("Data de transferência");
        configurarDatePickerPtBR(data);
        data.setValue(LocalDate.now());

        TextArea motivo = new TextArea("Motivo (opcional)");
        motivo.setWidthFull();
        motivo.setMaxLength(1000);

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.add(new Span("Aluno: " + (m.getAluno() != null ? m.getAluno().getNomeCompleto() : "")));
        form.add(turmaSelect, data, motivo);
        form.setColspan(motivo, 2);

        Button salvar = new Button("Transferir", e -> {
            try {
                Turma nova = turmaSelect.getValue();
                if (nova == null) {
                    Notification.show("Selecione a nova turma.", 3000, Notification.Position.MIDDLE);
                    return;
                }
                if (nova.getId().equals(m.getTurma().getId())) {
                    Notification.show("Selecione uma turma diferente da atual.", 3000, Notification.Position.MIDDLE);
                    return;
                }
                enrollmentService.transferEnrollment(m.getId(), nova.getId(), data.getValue(), motivo.getValue(), usuario);
                Notification.show("Transferência realizada.", 2500, Notification.Position.BOTTOM_START);
                d.close();
                refresh();
                if (onChanged != null) onChanged.run();
            } catch (Exception ex) {
                Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
            }
        });
        Button fechar = new Button("Fechar", e -> d.close());
        d.getFooter().add(new HorizontalLayout(salvar, fechar));

        d.add(form);
        d.open();
    }

    private String mapStatus(MatriculaStatus s) {
        if (s == null) return "";
        return switch (s) {
            case ATIVA -> "Ativa";
            case TRANSFERIDA -> "Transferida";
            case CANCELADA -> "Cancelada";
            case CONCLUIDA -> "Concluída";
        };
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

    private String msg(Exception ex) {
        return ex.getMessage() != null ? ex.getMessage() : "Erro inesperado";
    }
}