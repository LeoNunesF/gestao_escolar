package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Responsavel;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.service.escola.AlunoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

public class AddResponsavelDialog extends Dialog {

    private final AlunoService alunoService;
    private final Usuario usuario;
    private final Long alunoId;
    private final Runnable onSaved;

    // Modo edição
    private final boolean editMode;

    private final TextField nome = new TextField("Nome");
    private final TextField cpf = new TextField("CPF");
    private final TextField parentesco = new TextField("Parentesco");
    private final TextField rg = new TextField("RG");
    private final TextField telefone1 = new TextField("Telefone 1");
    private final TextField telefone2 = new TextField("Telefone 2");
    private final EmailField email = new EmailField("E-mail");
    private final TextField docGuarda = new TextField("Documento de guarda (opcional)");

    private final Checkbox papelLegal = new Checkbox("Responsável legal");
    private final Checkbox papelFinanceiro = new Checkbox("Responsável financeiro");
    private final Checkbox papelDidatico = new Checkbox("Responsável didático");

    private final Button salvar = new Button("Salvar");
    private final Button cancelar = new Button("Cancelar");

    // Construtor para ADICIONAR
    public AddResponsavelDialog(AlunoService alunoService, Usuario usuario, Long alunoId, Runnable onSaved) {
        this(alunoService, usuario, alunoId, onSaved, null, false, false, false);
    }

    // Construtor para EDITAR (responsável existente + papéis atuais)
    public AddResponsavelDialog(AlunoService alunoService,
                                Usuario usuario,
                                Long alunoId,
                                Runnable onSaved,
                                Responsavel existente,
                                boolean didatico,
                                boolean financeiro,
                                boolean legal) {
        this.alunoService = alunoService;
        this.usuario = usuario;
        this.alunoId = alunoId;
        this.onSaved = onSaved;
        this.editMode = (existente != null);

        setHeaderTitle(editMode ? "Editar Responsável" : "Adicionar Responsável");
        setWidth("840px");                 // maior
        getElement().getStyle().set("max-width", "90vw");
        setDraggable(true);
        setResizable(true);

        // Placeholders e restrições
        cpf.setPlaceholder("000.000.000-00");
        cpf.setAllowedCharPattern("[0-9.\\-]*");
        telefone1.setPlaceholder("(00) 00000-0000");
        telefone1.setAllowedCharPattern("[0-9()\\-\\s]*");
        telefone2.setPlaceholder("(00) 00000-0000");
        telefone2.setAllowedCharPattern("[0-9()\\-\\s]*");
        email.setClearButtonVisible(true);

        applyCpfMask(cpf);
        applyPhoneMask(telefone1);
        applyPhoneMask(telefone2);

        // Preenchimento em modo edição (CPF AGORA EDITÁVEL)
        if (editMode) {
            nome.setValue(nullToEmpty(existente.getNome()));
            cpf.setValue(formatCpf(existente.getCpf()));
            parentesco.setValue(nullToEmpty(existente.getParentesco()));
            rg.setValue(nullToEmpty(existente.getRg()));
            telefone1.setValue(formatPhone(existente.getTelefone1()));
            telefone2.setValue(formatPhone(existente.getTelefone2()));
            email.setValue(nullToEmpty(existente.getEmail()));
            docGuarda.setValue(nullToEmpty(existente.getDocGuarda()));

            papelDidatico.setValue(didatico);
            papelFinanceiro.setValue(financeiro);
            papelLegal.setValue(legal);
        }

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        form.add(nome, cpf, parentesco, rg, telefone1, telefone2, email, docGuarda);
        form.setColspan(docGuarda, 2);

        form.add(papelLegal, papelFinanceiro, papelDidatico);
        HorizontalLayout actions = new HorizontalLayout(salvar, cancelar);
        form.add(actions);
        form.setColspan(actions, 2);

        salvar.addClickListener(e -> onSave());
        cancelar.addClickListener(e -> close());

        add(form);
    }

    private void onSave() {
        try {
            Responsavel r = new Responsavel();
            r.setNome(nome.getValue());
            // persiste dígitos
            r.setCpf(normalizeDigits(cpf.getValue()));
            r.setParentesco(parentesco.getValue());
            r.setRg(rg.getValue());
            r.setTelefone1(normalizeDigits(telefone1.getValue()));
            r.setTelefone2(normalizeDigits(telefone2.getValue()));
            r.setEmail(email.getValue());
            r.setDocGuarda(docGuarda.getValue());

            alunoService.attachGuardian(
                    alunoId,
                    r,
                    papelDidatico.getValue(),
                    papelFinanceiro.getValue(),
                    papelLegal.getValue(),
                    usuario
            );
            Notification.show("Responsável salvo.", 2500, Notification.Position.BOTTOM_START);
            close();
            if (onSaved != null) onSaved.run();
        } catch (Exception ex) {
            Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    // ===== Helpers de máscara (null-safe) =====
    private void applyCpfMask(TextField field) {
        field.addFocusListener(e -> field.setValue(normalizeDigits(field.getValue())));
        field.addBlurListener(e -> field.setValue(formatCpf(field.getValue())));
    }

    private void applyPhoneMask(TextField field) {
        field.addFocusListener(e -> field.setValue(normalizeDigits(field.getValue())));
        field.addBlurListener(e -> field.setValue(formatPhone(field.getValue())));
    }

    private String normalizeDigits(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }

    private String formatCpf(String input) {
        if (input == null) return "";
        String d = normalizeDigits(input);
        if (d.length() != 11) return input.isEmpty() ? "" : input;
        return String.format("%s.%s.%s-%s",
                d.substring(0, 3),
                d.substring(3, 6),
                d.substring(6, 9),
                d.substring(9, 11));
    }

    private String formatPhone(String input) {
        if (input == null) return "";
        String d = normalizeDigits(input);
        if (d.length() == 11) {
            return String.format("(%s) %s-%s",
                    d.substring(0, 2),
                    d.substring(2, 7),
                    d.substring(7));
        } else if (d.length() == 10) {
            return String.format("(%s) %s-%s",
                    d.substring(0, 2),
                    d.substring(2, 6),
                    d.substring(6));
        }
        return input.isEmpty() ? "" : input;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}