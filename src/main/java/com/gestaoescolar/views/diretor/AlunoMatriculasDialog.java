package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Aluno;
import com.gestaoescolar.model.Matricula;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.MatriculaStatus;
import com.gestaoescolar.service.escola.EnrollmentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AlunoMatriculasDialog extends Dialog {

    private final EnrollmentService enrollmentService;
    private final Usuario usuario;
    private final Aluno aluno;

    private final Grid<Matricula> grid = new Grid<>(Matricula.class, false);
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AlunoMatriculasDialog(EnrollmentService enrollmentService,
                                 Usuario usuario,
                                 Aluno aluno) {
        this.enrollmentService = enrollmentService;
        this.usuario = usuario;
        this.aluno = aluno;

        setHeaderTitle("Matrículas de " + (aluno.getNomeCompleto() != null ? aluno.getNomeCompleto() : "Aluno"));
        setWidth("900px");
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

        grid.addColumn(m -> {
            Turma t = m.getTurma();
            if (t == null) return "";
            String codigo = t.getCodigo() != null ? t.getCodigo() : "";
            String nome = t.getNomeTurma() != null ? t.getNomeTurma() : "";
            return codigo + (nome.isBlank() ? "" : " - " + nome);
        }).setHeader("Turma").setAutoWidth(true);

        grid.addColumn(m -> {
            Turma t = m.getTurma();
            if (t == null || t.getAnoLetivo() == null) return "";
            Integer ano = t.getAnoLetivo().getAno();
            return ano != null ? ano.toString() : "";
        }).setHeader("Ano Letivo").setAutoWidth(true);

        grid.addColumn(m -> mapStatus(m.getStatus()))
                .setHeader("Status").setAutoWidth(true);

        grid.addColumn(m -> m.getDataInicio() != null ? df.format(m.getDataInicio()) : "")
                .setHeader("Início").setAutoWidth(true);

        grid.addColumn(m -> m.getDataTermino() != null ? df.format(m.getDataTermino()) : "")
                .setHeader("Término").setAutoWidth(true);

        grid.addColumn(m -> m.getMotivo() != null ? m.getMotivo() : "")
                .setHeader("Motivo").setAutoWidth(true);
    }

    private void refresh() {
        try {
            List<Matricula> list = enrollmentService.listEnrollmentsByStudent(aluno.getId(), usuario);
            grid.setItems(list);
            if (list.isEmpty()) {
                getHeader().add(new Span(" (sem matrículas)"));
            }
        } catch (Exception ex) {
            Notification.show(ex.getMessage() != null ? ex.getMessage() : "Erro ao carregar matrículas", 4000, Notification.Position.MIDDLE);
        }
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
}