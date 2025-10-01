package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.enums.StatusAnoLetivo;
import com.gestaoescolar.service.AnoLetivoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AnoLetivoForm extends Dialog {

    private final AnoLetivoService anoLetivoService;
    private final Runnable refreshCallback;

    private final IntegerField anoField = new IntegerField("Ano");
    private final DatePicker dataInicioField = new DatePicker("Data Início");
    private final DatePicker dataTerminoField = new DatePicker("Data Término");
    private final ComboBox<StatusAnoLetivo> statusField = new ComboBox<>("Status");
    private final TextArea observacoesField = new TextArea("Observações");

    private final Binder<AnoLetivo> binder = new Binder<>(AnoLetivo.class);

    public AnoLetivoForm(AnoLetivoService anoLetivoService, AnoLetivo anoLetivo, Runnable refreshCallback) {
        this.anoLetivoService = anoLetivoService;
        this.refreshCallback = refreshCallback;

        setHeaderTitle(anoLetivo.getId() == null ? "Novo Ano Letivo" : "Editar Ano Letivo");
        setWidth("600px");

        configureForm();
        createFormLayout();
        createButtons();

        binder.setBean(anoLetivo);
    }

    private void configureForm() {
        // Configurar locale brasileiro para os DatePickers
        configurarDatePickerBrasileiro(dataInicioField);
        configurarDatePickerBrasileiro(dataTerminoField);

        // Configurar campo de ano
        anoField.setMin(2000);
        anoField.setMax(2100);
        anoField.setStep(1);
        anoField.setRequiredIndicatorVisible(true);

        // Configurar campo de status
        statusField.setItems(Arrays.asList(StatusAnoLetivo.values()));
        statusField.setItemLabelGenerator(StatusAnoLetivo::getDescricao);
        statusField.setRequiredIndicatorVisible(true);

        dataInicioField.setRequiredIndicatorVisible(true);
        dataTerminoField.setRequiredIndicatorVisible(true);

        observacoesField.setMaxLength(500);
        observacoesField.setHeight("100px");

        // Binding manual
        binder.forField(anoField)
                .asRequired("Ano é obrigatório")
                .bind(AnoLetivo::getAno, AnoLetivo::setAno);

        binder.forField(dataInicioField)
                .asRequired("Data de início é obrigatória")
                .bind(AnoLetivo::getDataInicio, AnoLetivo::setDataInicio);

        binder.forField(dataTerminoField)
                .asRequired("Data de término é obrigatória")
                .bind(AnoLetivo::getDataTermino, AnoLetivo::setDataTermino);

        binder.forField(statusField)
                .asRequired("Status é obrigatório")
                .bind(AnoLetivo::getStatus, AnoLetivo::setStatus);

        binder.forField(observacoesField)
                .bind(AnoLetivo::getObservacoes, AnoLetivo::setObservacoes);
    }

    private void configurarDatePickerBrasileiro(DatePicker datePicker) {
        // Configurar para formato brasileiro
        datePicker.setLocale(new Locale("pt", "BR"));

        // Configurar placeholder no formato brasileiro
        datePicker.setPlaceholder("dd/mm/aaaa");

        // Configurar i18n manualmente - VERSÃO CORRIGIDA
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        // CORREÇÃO: Usar List.of() em vez de array []
        i18n.setMonthNames(List.of(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        ));

        i18n.setWeekdays(List.of(
                "Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira",
                "Quinta-feira", "Sexta-feira", "Sábado"
        ));

        i18n.setWeekdaysShort(List.of(
                "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"
        ));

        i18n.setToday("Hoje");
        i18n.setCancel("Cancelar");
        i18n.setFirstDayOfWeek(1); // Segunda-feira

        datePicker.setI18n(i18n);
    }

    private void createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(
                anoField, dataInicioField, dataTerminoField, statusField, observacoesField
        );
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        add(formLayout);
    }

    private void createButtons() {
        Button saveButton = new Button("Salvar", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", e -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        getFooter().add(buttons);
    }

    private void save() {
        if (binder.validate().isOk()) {
            try {
                anoLetivoService.save(binder.getBean());
                Notification.show("Ano letivo salvo com sucesso!", 3000, Notification.Position.MIDDLE);
                refreshCallback.run();
                close();
            } catch (IllegalArgumentException e) {
                Notification.show("Erro: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        }
    }
}