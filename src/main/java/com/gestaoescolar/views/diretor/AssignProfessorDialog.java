package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.service.escola.ProfessorTurmaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class AssignProfessorDialog extends Dialog {

    private final Turma turma;
    private final ProfessorService professorService;
    private final ProfessorTurmaService professorTurmaService;
    private final Usuario usuarioLogado;

    private final ComboBox<Professor> professorCombo = new ComboBox<>("Professor");
    private final TextField papelField = new TextField("Papel");
    private final TextField disciplinaField = new TextField("Disciplina");
    private final Button saveButton = new Button("Atribuir");
    private final Button cancelButton = new Button("Cancelar");

    public AssignProfessorDialog(Turma turma, 
                                 ProfessorService professorService,
                                 ProfessorTurmaService professorTurmaService,
                                 Usuario usuarioLogado) {
        this.turma = turma;
        this.professorService = professorService;
        this.professorTurmaService = professorTurmaService;
        this.usuarioLogado = usuarioLogado;

        setHeaderTitle("Atribuir Professor à Turma: " + turma.getCodigo());
        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("400px");

        configureFields();
        configureButtons();
        createLayout();
    }

    private void configureFields() {
        // Configurar combo de professores
        professorCombo.setItems(professorService.listarProfessoresAtivos(usuarioLogado));
        professorCombo.setItemLabelGenerator(Professor::getNomeFormatado);
        professorCombo.setPlaceholder("Selecione um professor");
        professorCombo.setWidthFull();
        professorCombo.setRequired(true);

        // Configurar campos de texto
        papelField.setPlaceholder("Ex: Titular, Auxiliar");
        papelField.setWidthFull();
        papelField.setClearButtonVisible(true);

        disciplinaField.setPlaceholder("Ex: Matemática, Português");
        disciplinaField.setWidthFull();
        disciplinaField.setClearButtonVisible(true);
    }

    private void configureButtons() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> assignProfessor());

        cancelButton.addClickListener(e -> close());
    }

    private void createLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        layout.add(professorCombo, papelField, disciplinaField);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setWidthFull();

        add(layout, buttonLayout);
    }

    private void assignProfessor() {
        // Validar campos obrigatórios
        if (professorCombo.isEmpty()) {
            Notification.show("Por favor, selecione um professor", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Professor professor = professorCombo.getValue();

        try {
            // Atribuir professor à turma
            professorTurmaService.assignProfessorToTurma(
                    professor.getId(),
                    turma.getId(),
                    papelField.getValue(),
                    disciplinaField.getValue(),
                    usuarioLogado
            );

            Notification.show(
                    "Professor " + professor.getNomeCompleto() + " atribuído com sucesso à turma " + turma.getCodigo(),
                    3000,
                    Notification.Position.BOTTOM_START
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            close();
        } catch (IllegalArgumentException ex) {
            Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            Notification.show("Erro inesperado: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
