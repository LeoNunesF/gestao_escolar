package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.TurmaPolicyOverride;
import com.gestaoescolar.model.EffectiveAcademicPolicy;
import com.gestaoescolar.model.enums.EvaluationScaleType;
import com.gestaoescolar.service.escola.AcademicPolicyService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.Optional;

public class TurmaPolicyDialog extends Dialog {

    private final AcademicPolicyService policyService;
    private final Turma turma;
    private final Runnable onChanged;

    private final Span resumoBase = new Span();
    private final ComboBox<String> escalaSelect = new ComboBox<>("Escala da turma");
    private final TextArea conceitos = new TextArea("Conceitos (separe com ponto e vírgula)");

    public TurmaPolicyDialog(AcademicPolicyService policyService,
                             Turma turma,
                             Runnable onChanged) {
        this.policyService = policyService;
        this.turma = turma;
        this.onChanged = onChanged;

        setHeaderTitle("Política da Turma — " + turma.getCodigo() + " - " + turma.getNomeTurma());
        setWidth("680px");

        configure();
        loadData();
    }

    private void configure() {
        // Resumo da política efetiva atual
        resumoBase.getElement().getThemeList().add("badge");

        // Opções: Herdar do Ano, Numérica, Conceitual
        escalaSelect.setItems("Herdar do Ano", "Numérica", "Conceitual");
        escalaSelect.addValueChangeListener(ev -> {
            String val = ev.getValue();
            conceitos.setVisible("Conceitual".equals(val));
        });

        conceitos.setPlaceholder("Ex.: Não Avaliado; Em Desenvolvimento; Satisfatório");
        conceitos.setWidthFull();

        FormLayout form = new FormLayout();
        form.add(resumoBase, escalaSelect, conceitos);
        form.setColspan(resumoBase, 2);
        form.setColspan(conceitos, 2);

        Button salvar = new Button("Salvar", e -> onSave());
        Button herdar = new Button("Herdar do Ano", e -> onInherit());
        Button fechar = new Button("Fechar", e -> close());

        getFooter().add(salvar, herdar, fechar);
        add(form);
    }

    private void loadData() {
        try {
            // Política efetiva para exibir resumo
            EffectiveAcademicPolicy eff = policyService.getEffectivePolicyForTurma(turma.getId());
            String escalaLabel = eff.getEvaluationScaleType() == EvaluationScaleType.CONCEITUAL ? "Conceitual" : "Numérica";
            resumoBase.setText("Efetiva: " + escalaLabel +
                    " | Periodicidade: " + (eff.getPeriodType() == com.gestaoescolar.model.enums.PeriodType.BIMESTRE ? "Bimestre" : "Trimestre") +
                    " | Frequência mínima: " + eff.getMinAttendancePercent() + "%");

            // Override atual
            Optional<TurmaPolicyOverride> ovOpt = policyService.getOverrideForTurma(turma.getId());
            if (ovOpt.isPresent()) {
                TurmaPolicyOverride ov = ovOpt.get();
                if (ov.getEvaluationScaleType() == EvaluationScaleType.CONCEITUAL) {
                    escalaSelect.setValue("Conceitual");
                    conceitos.setVisible(true);
                    conceitos.setValue(ov.getConceptLabels() != null ? ov.getConceptLabels() : (eff.getConceptLabels() != null ? eff.getConceptLabels() : ""));
                } else if (ov.getEvaluationScaleType() == EvaluationScaleType.NUMERICA) {
                    escalaSelect.setValue("Numérica");
                    conceitos.setVisible(false);
                    conceitos.clear();
                } else {
                    escalaSelect.setValue("Herdar do Ano");
                    conceitos.setVisible(false);
                    conceitos.clear();
                }
            } else {
                // Sem override: herda
                escalaSelect.setValue("Herdar do Ano");
                conceitos.setVisible(false);
                conceitos.clear();
            }
        } catch (Exception ex) {
            Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void onSave() {
        try {
            String choice = escalaSelect.getValue();
            if (choice == null) {
                Notification.show("Selecione a escala da turma.", 3000, Notification.Position.MIDDLE);
                return;
            }
            EvaluationScaleType type = null;
            String labels = null;
            if ("Numérica".equals(choice)) {
                type = EvaluationScaleType.NUMERICA;
            } else if ("Conceitual".equals(choice)) {
                type = EvaluationScaleType.CONCEITUAL;
                labels = conceitos.getValue();
            } else {
                // Herdar -> remover override
                policyService.removeTurmaOverride(turma.getId());
                Notification.show("Turma configurada para herdar a política do Ano Letivo.", 2500, Notification.Position.BOTTOM_START);
                if (onChanged != null) onChanged.run();
                close();
                return;
            }

            policyService.saveTurmaOverride(turma.getId(), type, labels);
            Notification.show("Política da turma atualizada.", 2500, Notification.Position.BOTTOM_START);
            if (onChanged != null) onChanged.run();
            close();
        } catch (Exception ex) {
            Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void onInherit() {
        try {
            policyService.removeTurmaOverride(turma.getId());
            Notification.show("Override removido. Herdando política do Ano Letivo.", 2500, Notification.Position.BOTTOM_START);
            if (onChanged != null) onChanged.run();
            close();
        } catch (Exception ex) {
            Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private String msg(Exception ex) {
        return ex.getMessage() != null ? ex.getMessage() : "Erro inesperado";
    }
}