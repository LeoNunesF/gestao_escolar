package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Aluno;
import com.gestaoescolar.model.AlunoResponsavel;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.Genero;
import com.gestaoescolar.service.escola.AlunoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class AlunoForm extends FormLayout {

    private final AlunoService alunoService;
    private final Usuario usuarioLogado;

    private Aluno aluno;

    // Abas
    private final Tabs tabs;
    private final Tab tabDados = new Tab("Dados");
    private final Tab tabDocumentos = new Tab("Documentos");
    private final Tab tabEndereco = new Tab("Endereço");
    private final Tab tabResponsaveis = new Tab("Responsáveis");
    private final Tab tabSaude = new Tab("Saúde");
    private final Tab tabObs = new Tab("Observações");

    // Dados
    private final TextField nomeCompleto = new TextField("Nome completo");
    private final TextField nomeSocial = new TextField("Nome social");
    private final DatePicker dataNascimento = new DatePicker("Data de nascimento");
    private final ComboBox<Genero> genero = new ComboBox<>("Gênero");
    private final TextField corRaca = new TextField("Cor/raça (opcional)");
    private final TextField telefone = new TextField("Telefone");
    private final EmailField email = new EmailField("E-mail");

    // Documentos
    private final TextField docTipo = new TextField("Tipo de documento (RA/Certidão/CPF)");
    private final TextField docNumero = new TextField("Número do documento");
    private final TextField cpf = new TextField("CPF (opcional)");
    private final TextField inep = new TextField("INEP (opcional)");
    private final TextField nis = new TextField("NIS (opcional)");
    private final TextArea justificativaDocumentos = new TextArea("Justificativa de documentos");

    // Endereço
    private final TextField cep = new TextField("CEP");
    private final TextField logradouro = new TextField("Logradouro");
    private final TextField numero = new TextField("Número");
    private final TextField complemento = new TextField("Complemento");
    private final TextField bairro = new TextField("Bairro");
    private final TextField cidade = new TextField("Cidade");
    private final TextField uf = new TextField("UF");

    // Saúde
    private final TextArea alergias = new TextArea("Alergias");
    private final TextArea observacoesSaude = new TextArea("Observações de saúde");

    // Observações
    private final TextArea observacoes = new TextArea("Observações (geral)");

    // Botões
    private final Button salvar = new Button("Salvar");
    private final Button fechar = new Button("Fechar");

    // Responsáveis (grid + botão)
    private final Grid<AlunoResponsavel> gridResponsaveis = new Grid<>(AlunoResponsavel.class, false);
    private final Button addResponsavel = new Button("Adicionar responsável");

    public AlunoForm(AlunoService alunoService, Usuario usuarioLogado) {
        this.alunoService = alunoService;
        this.usuarioLogado = usuarioLogado;

        // Layout do formulário: colado à esquerda, largura total com limite
        setWidthFull();
        getStyle().set("max-width", "980px"); // limite confortável
        getStyle().set("margin", "0");        // evita centralização

        tabs = new Tabs(tabDados, tabDocumentos, tabEndereco, tabResponsaveis, tabSaude, tabObs);
        add(new H3("Aluno"), tabs);

        // Configurar campos
        genero.setItems(Genero.values());

        // DatePicker em pt-BR (meses, dias, textos)
        configurarDatePickerPtBR(dataNascimento);

        // Placeholders e máscaras (formatação visual)
        configurarMascaras();

        email.setClearButtonVisible(true);
        justificativaDocumentos.setMaxLength(1000);
        observacoes.setMaxLength(4000);

        // Layout por aba (FormLayouts “grudados” à esquerda, 1–2 colunas)
        add(createDadosContent());
        add(createDocumentosContent());
        add(createEnderecoContent());
        add(createResponsaveisContent());
        add(createSaudeContent());
        add(createObsContent());
        add(createActions());

        // Alternar visualização por aba
        tabs.addSelectedChangeListener(e -> toggleTabs());

        salvar.addClickListener(e -> onSave());
        fechar.addClickListener(e -> fireEvent(new CloseEvent(this)));
        toggleTabs();
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

    private void configurarMascaras() {
        // Placeholders para orientar
        cpf.setPlaceholder("000.000.000-00");
        cep.setPlaceholder("00000-000");
        telefone.setPlaceholder("(00) 00000-0000");

        // Restringe a entrada a dígitos onde faz sentido
        cpf.setAllowedCharPattern("[0-9.\\-]*");
        cep.setAllowedCharPattern("[0-9\\-]*");
        telefone.setAllowedCharPattern("[0-9()\\-\\s]*");

        // Aplica formatação ao sair do campo e normaliza ao entrar
        applyCpfMask(cpf);
        applyCepMask(cep);
        applyPhoneMask(telefone);
    }

    private Component createDadosContent() {
        FormLayout box = new FormLayout();
        box.setWidthFull();
        box.getStyle().set("margin", "0");
        box.add(nomeCompleto, nomeSocial, dataNascimento, genero, corRaca, telefone, email);
        box.setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("900px", 2)
        );
        box.getElement().setProperty("data-tab", "Dados");
        return box;
    }

    private Component createDocumentosContent() {
        FormLayout box = new FormLayout();
        box.setWidthFull();
        box.getStyle().set("margin", "0");
        box.add(docTipo, docNumero, cpf, inep, nis, justificativaDocumentos);
        box.setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("900px", 2)
        );
        box.getElement().setProperty("data-tab", "Documentos");
        return box;
    }

    private Component createEnderecoContent() {
        FormLayout box = new FormLayout();
        box.setWidthFull();
        box.getStyle().set("margin", "0");
        box.add(cep, logradouro, numero, complemento, bairro, cidade, uf);
        box.setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("900px", 2)
        );
        box.getElement().setProperty("data-tab", "Endereço");
        return box;
    }

    // SUBSTITUA SOMENTE ESTE MÉTODO DENTRO DA CLASSE AlunoForm
    private Component createResponsaveisContent() {
        FormLayout box = new FormLayout();
        box.setWidthFull();
        box.getStyle().set("margin", "0");

        gridResponsaveis.addColumn(ar -> ar.getResponsavel().getNome()).setHeader("Nome").setAutoWidth(true);
        gridResponsaveis.addColumn(ar -> ar.getResponsavel().getCpf()).setHeader("CPF").setAutoWidth(true);
        gridResponsaveis.addColumn(ar -> ar.getResponsavel().getParentesco()).setHeader("Parentesco").setAutoWidth(true);
        gridResponsaveis.addColumn(ar -> ar.isResponsavelLegal() ? "Sim" : "Não").setHeader("Legal").setAutoWidth(true);
        gridResponsaveis.addColumn(ar -> ar.isResponsavelFinanceiro() ? "Sim" : "Não").setHeader("Financeiro").setAutoWidth(true);
        gridResponsaveis.addColumn(ar -> ar.isResponsavelDidatico() ? "Sim" : "Não").setHeader("Didático").setAutoWidth(true);

        // Coluna de ações: Editar + Remover
        gridResponsaveis.addComponentColumn(ar -> {
            Button editar = new Button("Editar", e -> {
                try {
                    var resp = ar.getResponsavel();
                    AddResponsavelDialog dlg = new AddResponsavelDialog(
                            alunoService,
                            usuarioLogado,
                            aluno.getId(),
                            this::refreshResponsaveis,
                            resp,
                            ar.isResponsavelDidatico(),
                            ar.isResponsavelFinanceiro(),
                            ar.isResponsavelLegal()
                    );
                    dlg.open();
                } catch (Exception ex) {
                    Notification.show("Erro ao abrir edição: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });

            Button remover = new Button("Remover", e -> {
                try {
                    alunoService.detachGuardian(aluno.getId(), ar.getResponsavel().getId(), usuarioLogado);
                    refreshResponsaveis();
                } catch (Exception ex) {
                    Notification.show("Erro ao remover responsável: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });
            return new HorizontalLayout(editar, remover);
        }).setHeader("Ações");

        addResponsavel.addClickListener(e -> {
            if (aluno == null || aluno.getId() == null) {
                Notification.show("Salve o aluno antes de adicionar responsáveis.", 3000, Notification.Position.MIDDLE);
                return;
            }
            AddResponsavelDialog dlg = new AddResponsavelDialog(alunoService, usuarioLogado, aluno.getId(), this::refreshResponsaveis);
            dlg.open();
        });

        Span titulo = new Span("Gerencie os responsáveis do aluno:");
        box.add(titulo, addResponsavel, gridResponsaveis);
        box.setColspan(titulo, 2);
        box.setColspan(addResponsavel, 2);
        box.setColspan(gridResponsaveis, 2);
        box.getElement().setProperty("data-tab", "Responsáveis");
        return box;
    }

    private Component createSaudeContent() {
        FormLayout box = new FormLayout();
        box.setWidthFull();
        box.getStyle().set("margin", "0");
        box.add(alergias, observacoesSaude);
        box.setResponsiveSteps(new ResponsiveStep("0", 1));
        box.getElement().setProperty("data-tab", "Saúde");
        return box;
    }

    private Component createObsContent() {
        FormLayout box = new FormLayout();
        box.setWidthFull();
        box.getStyle().set("margin", "0");
        box.add(observacoes);
        box.setResponsiveSteps(new ResponsiveStep("0", 1));
        box.getElement().setProperty("data-tab", "Observações");
        return box;
    }

    private Component createActions() {
        HorizontalLayout hl = new HorizontalLayout(salvar, fechar);
        hl.setWidthFull();
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        return hl;
    }

    private void toggleTabs() {
        String selected = tabs.getSelectedTab().getLabel();
        getChildren().forEach(c -> {
            if (c instanceof FormLayout fl) {
                String tab = fl.getElement().getProperty("data-tab");
                fl.setVisible(selected.equals(tab));
            }
        });
    }

    private void onSave() {
        try {
            if (aluno == null) aluno = new Aluno();
            aluno.setNomeCompleto(nomeCompleto.getValue());
            aluno.setNomeSocial(nomeSocial.getValue());
            aluno.setDataNascimento(dataNascimento.getValue());
            aluno.setGenero(genero.getValue());
            aluno.setCorRaca(corRaca.getValue());

            aluno.setDocTipo(docTipo.getValue());
            aluno.setDocNumero(docNumero.getValue());
            aluno.setCpf(normalizeDigits(cpf.getValue())); // salva somente dígitos
            aluno.setInep(inep.getValue());
            aluno.setNis(nis.getValue());
            aluno.setJustificativaDocumentos(justificativaDocumentos.getValue());

            aluno.setCep(normalizeDigits(cep.getValue())); // dígitos
            aluno.setLogradouro(logradouro.getValue());
            aluno.setNumero(numero.getValue());
            aluno.setComplemento(complemento.getValue());
            aluno.setBairro(bairro.getValue());
            aluno.setCidade(cidade.getValue());
            aluno.setUf(uf.getValue());

            aluno.setTelefone(normalizeDigits(telefone.getValue())); // dígitos
            aluno.setEmail(email.getValue());

            aluno.setAlergias(alergias.getValue());
            aluno.setObservacoesSaude(observacoesSaude.getValue());
            aluno.setObservacoes(observacoes.getValue());

            fireEvent(new SaveEvent(this, aluno));
        } catch (Exception ex) {
            Notification.show("Erro ao preparar dados: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    public void setAluno(Aluno a) {
        this.aluno = a;
        if (a == null) {
            clearFields();
            return;
        }
        nomeCompleto.setValue(nullToEmpty(a.getNomeCompleto()));
        nomeSocial.setValue(nullToEmpty(a.getNomeSocial()));
        dataNascimento.setValue(a.getDataNascimento() != null ? a.getDataNascimento() : LocalDate.now());
        genero.setValue(a.getGenero());
        corRaca.setValue(nullToEmpty(a.getCorRaca()));

        docTipo.setValue(nullToEmpty(a.getDocTipo()));
        docNumero.setValue(nullToEmpty(a.getDocNumero()));
        // Mostra formatado, mas persiste só dígitos
        cpf.setValue(formatCpf(a.getCpf()));
        inep.setValue(nullToEmpty(a.getInep()));
        nis.setValue(nullToEmpty(a.getNis()));
        justificativaDocumentos.setValue(nullToEmpty(a.getJustificativaDocumentos()));

        cep.setValue(formatCep(a.getCep()));
        logradouro.setValue(nullToEmpty(a.getLogradouro()));
        numero.setValue(nullToEmpty(a.getNumero()));
        complemento.setValue(nullToEmpty(a.getComplemento()));
        bairro.setValue(nullToEmpty(a.getBairro()));
        cidade.setValue(nullToEmpty(a.getCidade()));
        uf.setValue(nullToEmpty(a.getUf()));

        telefone.setValue(formatPhone(a.getTelefone()));
        email.setValue(nullToEmpty(a.getEmail()));

        alergias.setValue(nullToEmpty(a.getAlergias()));
        observacoesSaude.setValue(nullToEmpty(a.getObservacoesSaude()));

        observacoes.setValue(nullToEmpty(a.getObservacoes()));

        refreshResponsaveis();
    }

    private void refreshResponsaveis() {
        if (aluno == null || aluno.getId() == null) {
            gridResponsaveis.setItems(List.of());
            return;
        }
        try {
            gridResponsaveis.setItems(alunoService.listActiveGuardians(aluno.getId(), usuarioLogado));
        } catch (Exception ex) {
            Notification.show("Erro ao carregar responsáveis: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void clearFields() {
        nomeCompleto.clear();
        nomeSocial.clear();
        dataNascimento.clear();
        genero.clear();
        corRaca.clear();

        docTipo.clear();
        docNumero.clear();
        cpf.clear();
        inep.clear();
        nis.clear();
        justificativaDocumentos.clear();

        cep.clear();
        logradouro.clear();
        numero.clear();
        complemento.clear();
        bairro.clear();
        cidade.clear();
        uf.clear();

        telefone.clear();
        email.clear();

        alergias.clear();
        observacoesSaude.clear();
        observacoes.clear();

        gridResponsaveis.setItems(List.of());
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    // Eventos
    public static class SaveEvent extends ComponentEvent<AlunoForm> {
        private final Aluno aluno;

        public SaveEvent(AlunoForm source, Aluno aluno) {
            super(source, false);
            this.aluno = aluno;
        }

        public Aluno getAluno() {
            return aluno;
        }
    }

    public static class CloseEvent extends ComponentEvent<AlunoForm> {
        public CloseEvent(AlunoForm source) {
            super(source, false);
        }
    }

    // Listeners públicos (encapsulam o addListener protegido do Component)
    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }

    // =========================
    // Máscaras e formatação
    // =========================
    // ... dentro da classe AlunoForm ...

    // ===== Máscaras e formatação (versões null-safe) =====
    private void applyCpfMask(TextField field) {
        field.addFocusListener(e -> {
            field.setValue(normalizeDigits(field.getValue())); // nunca null
        });
        field.addBlurListener(e -> {
            field.setValue(formatCpf(field.getValue())); // exibe com máscara ou vazio
        });
    }

    private void applyCepMask(TextField field) {
        field.addFocusListener(e -> {
            field.setValue(normalizeDigits(field.getValue()));
        });
        field.addBlurListener(e -> {
            field.setValue(formatCep(field.getValue()));
        });
    }

    private void applyPhoneMask(TextField field) {
        field.addFocusListener(e -> {
            field.setValue(normalizeDigits(field.getValue()));
        });
        field.addBlurListener(e -> {
            field.setValue(formatPhone(field.getValue()));
        });
    }

    // Retorna sempre string ("" se null), sem caracteres não-numéricos
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

    private String formatCep(String input) {
        if (input == null) return "";
        String d = normalizeDigits(input);
        if (d.length() != 8) return input.isEmpty() ? "" : input;
        return String.format("%s-%s", d.substring(0, 5), d.substring(5));
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
}