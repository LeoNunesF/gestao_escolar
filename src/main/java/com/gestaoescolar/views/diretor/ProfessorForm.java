package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Endereco;
import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.FormacaoAcademica;
import com.gestaoescolar.model.enums.Genero;
import com.gestaoescolar.service.escola.ProfessorService;
import com.gestaoescolar.views.components.EnderecoForm;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * ProfessorForm com correção: garante que o CPF armazenado no entity esteja apenas com dígitos
 * (para satisfazer validação do modelo) e que o campo mostre CPF formatado ao usuário.
 */
public class ProfessorForm extends FormLayout {

    private TextField nomeCompleto = new TextField("Nome Completo");
    private TextField cpf = new TextField("CPF");
    private TextField rg = new TextField("RG");
    private TextField email = new TextField("Email");
    private TextField telefone = new TextField("Telefone");
    private DatePicker dataNascimento = new DatePicker("Data de Nascimento");
    private ComboBox<Genero> genero = new ComboBox<>("Gênero");
    private ComboBox<FormacaoAcademica> formacao = new ComboBox<>("Formação Acadêmica");
    private TextField especializacao = new TextField("Especialização");
    private DatePicker dataAdmissao = new DatePicker("Data de Admissão");
    private Checkbox ativo = new Checkbox("Ativo");

    // componente de endereço
    private EnderecoForm enderecoForm = new EnderecoForm();

    private Button salvar = new Button("Salvar");
    private Button cancelar = new Button("Cancelar");

    private Professor professor;
    private final ProfessorService professorService;
    private final Usuario usuarioLogado;

    private final Binder<Professor> binder = new Binder<>(Professor.class);
    private final Binder<Endereco> enderecoBinder = new Binder<>(Endereco.class);

    private static final Pattern TELEFONE_PATTERN = Pattern.compile("\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}");

    public ProfessorForm(ProfessorService professorService, Usuario usuarioLogado) {
        this.professorService = professorService;
        this.usuarioLogado = usuarioLogado;

        genero.setItems(Genero.values());
        formacao.setItems(FormacaoAcademica.values());

        // DatePickers em pt-BR
        configurarDatePickerBrasileiro(dataNascimento);
        configurarDatePickerBrasileiro(dataAdmissao);

        // binders
        configurarBinderProfessor();
        configurarBinderEndereco();

        // Formatação de telefone e CPF ao perder o foco
        telefone.addBlurListener(evt -> {
            String raw = telefone.getValue();
            if (raw != null && !raw.isBlank()) telefone.setValue(formatPhone(raw));
        });
        cpf.addBlurListener(evt -> {
            String raw = cpf.getValue();
            if (raw != null && !raw.isBlank()) cpf.setValue(formatCpf(raw));
        });

        salvar.addClickListener(e -> save());
        cancelar.addClickListener(e -> fireEvent(new CloseEvent(this)));

        HorizontalLayout botoes = new HorizontalLayout(salvar, cancelar);

        add(nomeCompleto, cpf, rg, email, telefone, dataNascimento, genero, formacao,
                especializacao, dataAdmissao, ativo, enderecoForm, botoes);
    }

    private void configurarBinderProfessor() {
        binder.forField(nomeCompleto)
                .asRequired("Nome completo é obrigatório")
                .bind(Professor::getNomeCompleto, Professor::setNomeCompleto);

        binder.forField(cpf)
                .asRequired("CPF é obrigatório")
                .withValidator(c -> c != null && !c.isBlank() && isValidCPF(c), "CPF inválido")
                .bind(Professor::getCpf, Professor::setCpf);

        binder.forField(rg)
                .asRequired("RG é obrigatório")
                .bind(Professor::getRg, Professor::setRg);

        binder.forField(email)
                .withValidator(e -> e == null || e.isBlank() || e.contains("@"), "Email inválido")
                .bind(Professor::getEmail, Professor::setEmail);

        binder.forField(telefone)
                .withValidator(t -> t == null || t.isBlank() || TELEFONE_PATTERN.matcher(t).matches(),
                        "Telefone deve seguir o padrão (11) 99999-9999 ou (11) 9999-9999")
                .bind(Professor::getTelefone, Professor::setTelefone);

        binder.forField(dataNascimento)
                .asRequired("Data de nascimento é obrigatória")
                .withValidator(d -> d == null || d.isBefore(java.time.LocalDate.now()), "Data inválida")
                .bind(Professor::getDataNascimento, Professor::setDataNascimento);

        binder.forField(dataAdmissao)
                .bind(Professor::getDataAdmissao, Professor::setDataAdmissao);

        binder.forField(genero)
                .asRequired("Gênero é obrigatório")
                .bind(Professor::getGenero, Professor::setGenero);

        binder.forField(formacao)
                .asRequired("Formação acadêmica é obrigatória")
                .bind(Professor::getFormacao, Professor::setFormacao);

        binder.forField(especializacao)
                .bind(Professor::getEspecializacao, Professor::setEspecializacao);

        binder.forField(ativo)
                .bind(Professor::isAtivo, Professor::setAtivo);
    }

    private void configurarBinderEndereco() {
        // usa os getters do EnderecoForm
        enderecoBinder.forField(enderecoForm.getCepField())
                .withValidator(cep -> cep == null || cep.isBlank() || enderecoForm.isCepValido(),
                        "CEP inválido (formato 00000-000)")
                .bind(com.gestaoescolar.model.Endereco::getCep, com.gestaoescolar.model.Endereco::setCep);

        enderecoBinder.forField(enderecoForm.getLogradouroField())
                .bind(com.gestaoescolar.model.Endereco::getLogradouro, com.gestaoescolar.model.Endereco::setLogradouro);

        enderecoBinder.forField(enderecoForm.getNumeroField())
                .bind(com.gestaoescolar.model.Endereco::getNumero, com.gestaoescolar.model.Endereco::setNumero);

        enderecoBinder.forField(enderecoForm.getComplementoField())
                .bind(com.gestaoescolar.model.Endereco::getComplemento, com.gestaoescolar.model.Endereco::setComplemento);

        enderecoBinder.forField(enderecoForm.getBairroField())
                .bind(com.gestaoescolar.model.Endereco::getBairro, com.gestaoescolar.model.Endereco::setBairro);

        enderecoBinder.forField(enderecoForm.getCidadeField())
                .bind(com.gestaoescolar.model.Endereco::getCidade, com.gestaoescolar.model.Endereco::setCidade);

        enderecoBinder.forField(enderecoForm.getEstadoField())
                .bind(com.gestaoescolar.model.Endereco::getEstado, com.gestaoescolar.model.Endereco::setEstado);

        enderecoBinder.setBean(new com.gestaoescolar.model.Endereco());
    }

    private void save() {
        if (professor == null) {
            professor = new Professor();
        }

        // Primeiro valida/escreve o endereço
        com.gestaoescolar.model.Endereco enderecoAtual = enderecoBinder.getBean();
        boolean enderecoOk = enderecoBinder.writeBeanIfValid(enderecoAtual);
        if (!enderecoOk) {
            Notification.show("Corrija os erros no endereço.", 3000, Notification.Position.MIDDLE);
            return;
        }

        // associa endereço ao professor (antes de validar o professor)
        professor.setEndereco(enderecoAtual);

        // Valida e escreve os campos do professor no bean
        boolean professorOk = binder.writeBeanIfValid(professor);
        if (!professorOk) {
            Notification.show("Corrija os erros no formulário do professor.", 3000, Notification.Position.MIDDLE);
            return;
        }

        // CORREÇÃO CRUCIAL: garantir que o CPF armazenado seja apenas dígitos (sem máscara)
        String cpfFieldValue = cpf.getValue();
        if (cpfFieldValue != null && !cpfFieldValue.isBlank()) {
            professor.setCpf(onlyDigits(cpfFieldValue));
        }

        // Dispara o evento para a view salvar via ProfessorService
        fireEvent(new SaveEvent(this, professor));
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
        if (professor != null) {
            // Carrega dados no binder
            binder.readBean(professor);

            // Se houver cpf no model (possivelmente sem formatação), exibir formatado no campo
            String cpfValor = professor.getCpf();
            if (cpfValor != null && !cpfValor.isBlank()) {
                cpf.setValue(formatCpf(cpfValor));
            }

            if (professor.getEndereco() != null) {
                enderecoBinder.setBean(professor.getEndereco());
                enderecoForm.setEndereco(professor.getEndereco());
            } else {
                enderecoBinder.setBean(new com.gestaoescolar.model.Endereco());
                enderecoForm.setEndereco(null);
            }

            setVisible(true);
        } else {
            binder.readBean(new Professor());
            enderecoBinder.setBean(new com.gestaoescolar.model.Endereco());
            enderecoForm.setEndereco(null);
            cpf.clear();
            setVisible(false);
        }
    }

    // Helpers de formatação
    private String onlyDigits(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }

    private String formatPhone(String input) {
        String digits = onlyDigits(input);
        if (digits.length() == 11) {
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 7),
                    digits.substring(7, 11));
        } else if (digits.length() == 10) {
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 6),
                    digits.substring(6, 10));
        } else {
            return input;
        }
    }

    private String formatCpf(String input) {
        String d = onlyDigits(input);
        if (d.length() == 11) {
            return String.format("%s.%s.%s-%s",
                    d.substring(0,3),
                    d.substring(3,6),
                    d.substring(6,9),
                    d.substring(9,11));
        }
        return input;
    }

    /**
     * Validador completo de CPF (verifica dígitos verificadores).
     */
    private boolean isValidCPF(String cpfInput) {
        String cpf = onlyDigits(cpfInput);
        if (cpf.length() != 11) return false;
        // rejeita sequências com todos os dígitos iguais
        if (cpf.chars().distinct().count() == 1) return false;

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (cpf.charAt(i) - '0') * (10 - i);
            }
            int r = sum % 11;
            int dv1 = (r < 2) ? 0 : 11 - r;
            if (dv1 != (cpf.charAt(9) - '0')) return false;

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += (cpf.charAt(i) - '0') * (11 - i);
            }
            r = sum % 11;
            int dv2 = (r < 2) ? 0 : 11 - r;
            return dv2 == (cpf.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    private void configurarDatePickerBrasileiro(DatePicker datePicker) {
        datePicker.setLocale(new Locale("pt", "BR"));
        datePicker.setPlaceholder("dd/mm/aaaa");
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
        datePicker.setI18n(i18n);
    }

    // Eventos
    public static abstract class ProfessorFormEvent extends ComponentEvent<ProfessorForm> {
        private final Professor professor;

        protected ProfessorFormEvent(ProfessorForm source, Professor professor) {
            super(source, false);
            this.professor = professor;
        }

        public Professor getProfessor() {
            return professor;
        }
    }

    public static class SaveEvent extends ProfessorFormEvent {
        SaveEvent(ProfessorForm source, Professor professor) {
            super(source, professor);
        }
    }

    public static class CloseEvent extends ProfessorFormEvent {
        CloseEvent(ProfessorForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}