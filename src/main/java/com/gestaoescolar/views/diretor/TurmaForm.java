package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.Serie;
import com.gestaoescolar.model.enums.Turno;
import com.gestaoescolar.service.escola.TurmaService;
import com.gestaoescolar.service.AnoLetivoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.Arrays;

public class TurmaForm extends Dialog {

    private final TurmaService turmaService;
    private final AnoLetivoService anoLetivoService;
    private final Usuario usuarioLogado;

    private final Runnable refreshCallback;

    // Campos do formulário
    private final TextField nomeTurmaField = new TextField("Nome da Turma");
    private final ComboBox<Serie> serieField = new ComboBox<>("Série");
    private final ComboBox<Turno> turnoField = new ComboBox<>("Turno");
    private final ComboBox<AnoLetivo> anoLetivoField = new ComboBox<>("Ano Letivo");
    private final IntegerField capacidadeField = new IntegerField("Capacidade");
    private final TextField salaField = new TextField("Sala");

    // Campo de código (apenas leitura)
    private final TextField codigoField = new TextField("Código da Turma");
    private final Span nivelInfo = new Span();

    private final Binder<Turma> binder = new Binder<>(Turma.class);

    public TurmaForm(TurmaService turmaService, AnoLetivoService anoLetivoService, Usuario usuarioLogado,
                     Turma turma, Runnable refreshCallback) {
        this.turmaService = turmaService;
        this.anoLetivoService = anoLetivoService;
        this.refreshCallback = refreshCallback;
        this.usuarioLogado = usuarioLogado;


        setHeaderTitle(turma.getId() == null ? "Nova Turma" : "Editar Turma");
        setWidth("700px");
        setModal(true);
        setCloseOnOutsideClick(false);

        configureForm();
        createFormLayout();
        createButtons();

        binder.setBean(turma);

        // Atualizar código e nível quando campos mudarem
        setupFieldListeners();

        // Atualizar campos iniciais
        updateCodigoENivel();
    }

    private void configureForm() {
        // Configurar campos
        nomeTurmaField.setRequiredIndicatorVisible(true);
        nomeTurmaField.setMinLength(1);
        nomeTurmaField.setMaxLength(20);
        nomeTurmaField.setPlaceholder("Ex: A, B, Mercúrio, Vênus");

        serieField.setItems(Arrays.asList(Serie.values()));
        serieField.setItemLabelGenerator(Serie::getNome);
        serieField.setRequiredIndicatorVisible(true);

        turnoField.setItems(Arrays.asList(Turno.values()));
        turnoField.setItemLabelGenerator(Turno::getDescricao);
        turnoField.setRequiredIndicatorVisible(true);

        // CORREÇÃO: Usar findAll() que existe no AnoLetivoService
        anoLetivoField.setItems(anoLetivoService.findAll());
        anoLetivoField.setItemLabelGenerator(ano -> "Ano " + ano.getAno());
        anoLetivoField.setRequiredIndicatorVisible(true);

        capacidadeField.setMin(1);
        capacidadeField.setMax(50);
        capacidadeField.setStep(1);
        capacidadeField.setHelperText("Número máximo de alunos");

        salaField.setMaxLength(10);
        salaField.setPlaceholder("Ex: 101, B12");

        codigoField.setReadOnly(true);
        codigoField.setHelperText("Gerado automaticamente");

        nivelInfo.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Configurar validações do binder
        binder.forField(nomeTurmaField)
                .asRequired("Nome da turma é obrigatório")
                .withValidator(nome -> !nome.isEmpty(), "Nome deve ter pelo menos 1 caractere")
                .bind(Turma::getNomeTurma, Turma::setNomeTurma);

        binder.forField(serieField)
                .asRequired("Série é obrigatória")
                .bind(Turma::getSerie, Turma::setSerie);

        binder.forField(turnoField)
                .asRequired("Turno é obrigatório")
                .bind(Turma::getTurno, Turma::setTurno);

        binder.forField(anoLetivoField)
                .asRequired("Ano letivo é obrigatório")
                .bind(Turma::getAnoLetivo, Turma::setAnoLetivo);

        binder.forField(capacidadeField)
                .withValidator(capacidade -> capacidade == null || capacidade > 0,
                        "Capacidade deve ser maior que zero")
                .bind(Turma::getCapacidade, Turma::setCapacidade);

        binder.forField(salaField)
                .bind(Turma::getSala, Turma::setSala);

        binder.forField(codigoField)
                .asRequired("Código da turma é obrigatório")
                .bind(Turma::getCodigo, Turma::setCodigo);
    }

    private void setupFieldListeners() {
        // Atualizar código e nível quando campos relacionados mudarem
        nomeTurmaField.addValueChangeListener(e -> updateCodigoENivel());
        serieField.addValueChangeListener(e -> updateCodigoENivel());
        turnoField.addValueChangeListener(e -> updateCodigoENivel());
        anoLetivoField.addValueChangeListener(e -> updateCodigoENivel());

        // Atualizar nível quando série mudar
        serieField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                String nivel = e.getValue().getNome().contains("Maternal") ?
                        "Educação Infantil" :
                        e.getValue().getNome().contains("Série") ?
                                "Ensino Médio" : "Ensino Fundamental";
                nivelInfo.setText("Nível: " + nivel);
            }
        });
    }

    private void updateCodigoENivel() {
        Turma turma = binder.getBean();
        if (turma != null) {
            turma.gerarCodigoAutomatico();
            codigoField.setValue(turma.getCodigo() != null ? turma.getCodigo() : "");
        }
    }

    private void createFormLayout() {
        FormLayout formLayout = new FormLayout();

        // Adicionar info do nível abaixo da série
        HorizontalLayout serieLayout = new HorizontalLayout(serieField, nivelInfo);
        serieLayout.setAlignItems(FlexComponent.Alignment.END);
        serieLayout.setSpacing(true);

        // Adicionar todos os campos ao form layout
        formLayout.add(
                nomeTurmaField,
                serieLayout,  // Usar o layout customizado da série
                turnoField,
                anoLetivoField,
                capacidadeField,
                salaField,
                codigoField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        add(formLayout);
    }

    private void createButtons() {
        Button salvarButton = new Button("Salvar", e -> salvarTurma());
        salvarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarButton = new Button("Cancelar", e -> close());

        HorizontalLayout buttons = new HorizontalLayout(salvarButton, cancelarButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        getFooter().add(buttons);
    }

    private void salvarTurma() {
        if (binder.validate().isOk()) {
            try {
                Turma turma = binder.getBean();

                if (turma.getId() == null) {
                    // NOVA TURMA - CORREÇÃO: usar criarTurma (sem espaço)
                    turmaService.criarTurma(turma, usuarioLogado); // Usuario será injetado depois
                    Notification.show("Turma criada com sucesso!");
                } else {
                    // EDIÇÃO DE TURMA - CORREÇÃO: usar atualizarTurma (sem espaço)
                    turmaService.atualizarTurma(turma.getId(), turma, usuarioLogado); // Usuario será injetado depois
                    Notification.show("Turma atualizada com sucesso!");
                }

                refreshCallback.run();
                close();

            } catch (SecurityException e) {
                Notification.show("Erro de permissão: " + e.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            } catch (IllegalArgumentException e) {
                Notification.show("Erro de validação: " + e.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Erro ao salvar turma: " + e.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Corrija os erros antes de salvar");
        }
    }
}