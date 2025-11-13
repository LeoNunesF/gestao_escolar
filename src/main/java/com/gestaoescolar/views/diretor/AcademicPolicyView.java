package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.AcademicPeriod;
import com.gestaoescolar.model.AcademicPolicy;
import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.model.enums.AcademicRoundingMode;
import com.gestaoescolar.model.enums.EvaluationScaleType;
import com.gestaoescolar.model.enums.EvaluationWeightingMode;
import com.gestaoescolar.model.enums.PeriodType;
import com.gestaoescolar.model.enums.RecoveryRule;
import com.gestaoescolar.service.AnoLetivoService;
import com.gestaoescolar.service.escola.AcademicPolicyService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route(value = "diretor/politica-academica", layout = MainLayout.class)
@PageTitle("Política Acadêmica | Gestão Escolar")
public class AcademicPolicyView extends VerticalLayout {

    private final AcademicPolicyService policyService;
    private final AnoLetivoService anoLetivoService;

    private final ComboBox<AnoLetivo> anoSelect = new ComboBox<>("Ano Letivo");

    private final ComboBox<EvaluationScaleType> scaleType = new ComboBox<>("Escala de avaliação");
    private final TextArea conceptLabels = new TextArea("Conceitos (separe com ponto e vírgula)");

    private final ComboBox<PeriodType> periodType = new ComboBox<>("Periodicidade");
    private final IntegerField totalPeriods = new IntegerField("Qtd. de períodos");

    private final NumberField scaleMin = new NumberField("Nota mínima da escala");
    private final NumberField scaleMax = new NumberField("Nota máxima da escala");
    private final IntegerField decimalPrecision = new IntegerField("Casas decimais (exibição)");
    private final ComboBox<AcademicRoundingMode> rounding = new ComboBox<>("Arredondamento");
    private final NumberField minAverage = new NumberField("Média mínima de aprovação");
    private final IntegerField minAttendance = new IntegerField("Frequência mínima (%)");
    private final ComboBox<EvaluationWeightingMode> evalWeighting = new ComboBox<>("Modelo de avaliação");
    private final NumberField totalPointsPerPeriod = new NumberField("Pontos por período");
    private final ComboBox<RecoveryRule> recoveryRule = new ComboBox<>("Regra de recuperação");

    private final Grid<AcademicPeriod> gridPeriods = new Grid<>(AcademicPeriod.class, false);

    // Bloco recolhível para configurações numéricas
    private Details numericDetails;

    private AcademicPolicy editing;

    public AcademicPolicyView(AcademicPolicyService policyService,
                              AnoLetivoService anoLetivoService) {
        this.policyService = policyService;
        this.anoLetivoService = anoLetivoService;

        setSizeFull();
        setPadding(true);
        add(new H2("Política Acadêmica"));

        // Formulário dentro de Scroller para manter o grid visível
        Scroller formScroller = new Scroller(createHeaderForm());
        formScroller.setHeight("42vh");
        formScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        // Grid ocupa o restante da tela
        gridPeriods.setHeight("46vh");

        add(formScroller, gridPeriods);
        setFlexGrow(0, formScroller);
        setFlexGrow(1, gridPeriods);

        configureGrid();
        updateAnoLetivoItems();
    }

    private FormLayout createHeaderForm() {
        FormLayout form = new FormLayout();

        // Ano letivo
        anoSelect.setItemLabelGenerator(a -> a.getAno() != null ? a.getAno().toString() : "Ano");
        anoSelect.setWidth("240px");
        anoSelect.addValueChangeListener(ev -> onAnoLetivoChange());

        // Escala de avaliação
        scaleType.setItems(EvaluationScaleType.values());
        scaleType.setItemLabelGenerator(this::label);
        scaleType.addValueChangeListener(ev -> updateVisibilityByScaleType());
        conceptLabels.setPlaceholder("Ex.: Não Avaliado; Em Desenvolvimento; Satisfatório");
        conceptLabels.setWidthFull();

        // Periodicidade
        periodType.setItems(PeriodType.values());
        periodType.setItemLabelGenerator(this::label);
        periodType.addValueChangeListener(ev -> {
            if (ev.getValue() == PeriodType.BIMESTRE) {
                if (totalPeriods.getValue() == null || totalPeriods.getValue() == 3) totalPeriods.setValue(4);
            } else if (ev.getValue() == PeriodType.TRIMESTRE) {
                if (totalPeriods.getValue() == null || totalPeriods.getValue() == 4) totalPeriods.setValue(3);
            }
        });
        totalPeriods.setMin(1);
        totalPeriods.setMax(6);
        totalPeriods.setStepButtonsVisible(true);

        // Campos numéricos (agrupados em um bloco recolhível)
        scaleMin.setStep(0.1);
        scaleMax.setStep(0.1);
        scaleMin.setWidth("160px");
        scaleMax.setWidth("160px");
        decimalPrecision.setMin(0);
        decimalPrecision.setMax(3);
        decimalPrecision.setStepButtonsVisible(true);
        rounding.setItems(AcademicRoundingMode.values());
        rounding.setItemLabelGenerator(this::label);
        minAverage.setStep(0.1);
        minAverage.setWidth("200px");
        evalWeighting.setItems(EvaluationWeightingMode.values());
        evalWeighting.setItemLabelGenerator(this::label);
        totalPointsPerPeriod.setStep(0.5);
        recoveryRule.setItems(RecoveryRule.values());
        recoveryRule.setItemLabelGenerator(this::label);

        // Frequência (sempre visível)
        minAttendance.setMin(0);
        minAttendance.setMax(100);
        minAttendance.setStepButtonsVisible(true);

        // Botões
        Button salvar = new Button("Salvar configurações", e -> onSavePolicy());
        Button gerar = new Button("Gerar períodos padrão", e -> onRegeneratePeriods());
        HorizontalLayout actions = new HorizontalLayout(salvar, gerar);

        // Monta bloco numérico em um sub-form
        FormLayout numericForm = new FormLayout();
        numericForm.add(
                scaleMin, scaleMax, decimalPrecision, rounding,
                minAverage, evalWeighting, totalPointsPerPeriod, recoveryRule
        );
        numericForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        numericDetails = new Details("Configurações numéricas", numericForm);
        numericDetails.setOpened(false); // recolhido por padrão

        // Form principal
        form.add(
                anoSelect,
                scaleType, conceptLabels,
                periodType, totalPeriods,
                minAttendance,
                numericDetails,
                actions
        );

        form.setColspan(conceptLabels, 2);
        form.setColspan(numericDetails, 2);
        form.setColspan(actions, 2);

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("900px", 2)
        );

        return form;
    }

    private void configureGrid() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        gridPeriods.addColumn(AcademicPeriod::getIndexNumber).setHeader("#").setAutoWidth(true);
        gridPeriods.addColumn(AcademicPeriod::getName).setHeader("Período").setAutoWidth(true);
        gridPeriods.addColumn(p -> p.getStartDate() != null ? df.format(p.getStartDate()) : "")
                .setHeader("Início").setAutoWidth(true);
        gridPeriods.addColumn(p -> p.getEndDate() != null ? df.format(p.getEndDate()) : "")
                .setHeader("Fim").setAutoWidth(true);

        gridPeriods.addComponentColumn(p -> {
            Button editar = new Button("Editar datas", e -> openEditPeriodDialog(p));
            return editar;
        }).setHeader("Ações");
    }

    private void openEditPeriodDialog(AcademicPeriod p) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Editar período " + p.getIndexNumber());

        DatePicker di = new DatePicker("Início");
        DatePicker df = new DatePicker("Fim");
        configurarDatePickerPtBR(di);
        configurarDatePickerPtBR(df);

        di.setValue(p.getStartDate());
        df.setValue(p.getEndDate());

        Button salvar = new Button("Salvar", ev -> {
            try {
                LocalDate ini = di.getValue();
                LocalDate fim = df.getValue();
                if (ini != null && fim != null && fim.isBefore(ini)) {
                    Notification.show("Data fim não pode ser anterior à data de início.", 3000, Notification.Position.MIDDLE);
                    return;
                }
                policyService.updatePeriod(p.getId(), p.getName(), ini, fim);
                Notification.show("Período atualizado.", 2000, Notification.Position.BOTTOM_START);
                reloadPeriods();
                d.close();
            } catch (Exception ex) {
                Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
            }
        });
        Button fechar = new Button("Fechar", ev -> d.close());

        HorizontalLayout footer = new HorizontalLayout(salvar, fechar);
        d.add(di, df);
        d.getFooter().add(footer);
        d.setWidth("420px");
        d.open();
    }

    private void onAnoLetivoChange() {
        AnoLetivo ano = anoSelect.getValue();
        if (ano == null) {
            editing = null;
            gridPeriods.setItems(List.of());
            return;
        }
        try {
            AcademicPolicy p = policyService.getOrCreatePolicy(ano.getId());
            editing = p;

            scaleType.setValue(p.getEvaluationScaleType());
            conceptLabels.setValue(p.getConceptLabels() != null ? p.getConceptLabels() : "");

            periodType.setValue(p.getPeriodType());
            totalPeriods.setValue(p.getTotalPeriods());

            scaleMin.setValue(p.getScaleMin());
            scaleMax.setValue(p.getScaleMax());
            decimalPrecision.setValue(p.getDecimalPrecision());
            rounding.setValue(p.getRoundingMode());
            minAverage.setValue(p.getMinAverageForApproval());
            minAttendance.setValue(p.getMinAttendancePercent());
            evalWeighting.setValue(p.getEvaluationWeighting());
            totalPointsPerPeriod.setValue(p.getTotalPointsPerPeriod());
            recoveryRule.setValue(p.getRecoveryRule());

            updateVisibilityByScaleType();
            reloadPeriods();
        } catch (Exception ex) {
            Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void onSavePolicy() {
        if (editing == null || editing.getAnoLetivo() == null) {
            Notification.show("Selecione um ano letivo.", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            AcademicPolicy temp = new AcademicPolicy();
            temp.setAnoLetivo(editing.getAnoLetivo());

            temp.setEvaluationScaleType(scaleType.getValue());
            temp.setConceptLabels(conceptLabels.getValue());

            temp.setPeriodType(periodType.getValue());
            temp.setTotalPeriods(totalPeriods.getValue());

            temp.setScaleMin(scaleMin.getValue());
            temp.setScaleMax(scaleMax.getValue());
            temp.setDecimalPrecision(decimalPrecision.getValue());
            temp.setRoundingMode(rounding.getValue());
            temp.setMinAverageForApproval(minAverage.getValue());
            temp.setMinAttendancePercent(minAttendance.getValue());
            temp.setEvaluationWeighting(evalWeighting.getValue());
            temp.setTotalPointsPerPeriod(totalPointsPerPeriod.getValue());
            temp.setRecoveryRule(recoveryRule.getValue());

            editing = policyService.savePolicy(temp);
            Notification.show("Configurações salvas.", 2000, Notification.Position.BOTTOM_START);
        } catch (Exception ex) {
            Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void onRegeneratePeriods() {
        if (editing == null || editing.getId() == null) {
            Notification.show("Selecione um ano letivo.", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            policyService.regenerateDefaultPeriods(editing.getId());
            Notification.show("Períodos gerados conforme a periodicidade.", 2500, Notification.Position.BOTTOM_START);
            reloadPeriods();
        } catch (Exception ex) {
            Notification.show(msg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void reloadPeriods() {
        if (editing != null && editing.getId() != null) {
            List<AcademicPeriod> periods = policyService.listPeriods(editing.getId());
            gridPeriods.setItems(periods);
        } else {
            gridPeriods.setItems(List.of());
        }
    }

    private void updateAnoLetivoItems() {
        anoSelect.setItems(anoLetivoService.findAll()); // ajuste para o método da sua service
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

    // Rótulos pt-BR para os enums
    private String label(PeriodType t) {
        if (t == null) return "";
        return switch (t) {
            case BIMESTRE -> "Bimestre";
            case TRIMESTRE -> "Trimestre";
        };
    }

    private String label(AcademicRoundingMode m) {
        if (m == null) return "";
        return switch (m) {
            case HALF_UP -> "Padrão (half up)";
            case HALF_EVEN -> "Bancário (half even)";
            case UP -> "Para cima";
            case DOWN -> "Para baixo";
        };
    }

    private String label(EvaluationWeightingMode m) {
        if (m == null) return "";
        return switch (m) {
            case EQUAL -> "Pesos iguais";
            case BY_WEIGHT -> "Por pesos";
        };
    }

    private String label(RecoveryRule r) {
        if (r == null) return "";
        return switch (r) {
            case NONE -> "Sem recuperação";
            case REPLACE_PERIOD_AVG_IF_HIGHER -> "Substitui média do período se maior";
        };
    }

    private String label(EvaluationScaleType s) {
        if (s == null) return "";
        return switch (s) {
            case NUMERICA -> "Numérica";
            case CONCEITUAL -> "Conceitual";
        };
    }

    private void updateVisibilityByScaleType() {
        EvaluationScaleType st = scaleType.getValue();
        boolean isConceitual = st == EvaluationScaleType.CONCEITUAL;

        conceptLabels.setVisible(isConceitual);

        // Mostra/esconde bloco numérico inteiro
        numericDetails.setVisible(!isConceitual);

        // Sempre visíveis
        periodType.setVisible(true);
        totalPeriods.setVisible(true);
        minAttendance.setVisible(true);
    }

    private String msg(Exception ex) {
        return ex.getMessage() != null ? ex.getMessage() : "Erro inesperado";
    }
}