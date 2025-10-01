package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.AnoLetivo;
import com.gestaoescolar.service.AnoLetivoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


import com.gestaoescolar.views.shared.MainLayout;


@Route(value = "diretor/ano-letivo", layout = MainLayout.class) // ← Adicionar layout
@PageTitle("Ano Letivo | Gestão Escolar")
public class AnoLetivoView extends VerticalLayout {

    private final AnoLetivoService anoLetivoService;
    private final Grid<AnoLetivo> grid = new Grid<>(AnoLetivo.class);

    public AnoLetivoView(AnoLetivoService anoLetivoService) {
        this.anoLetivoService = anoLetivoService;

        setSizeFull();
        setPadding(true);

        // Cabeçalho simples para esta view
        H1 header = new H1("Sistema de Gestão Escolar - Administração");
        header.getStyle()
                .set("background", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("padding", "var(--lumo-space-m)")
                .set("margin", "0")
                .set("width", "100%");

        add(header);
        add(new H2("Gerenciamento de Anos Letivos"));
        add(createToolbar());
        configureGrid();
        add(grid);

        updateList();
    }

    private HorizontalLayout createToolbar() {
        Button addButton = new Button("Novo Ano Letivo", new Icon(VaadinIcon.PLUS));

        // VERSÃO SIMPLIFICADA E TESTADA
        addButton.addClickListener(e -> {
            System.out.println("✅ Botão clicado - criando diálogo...");

            AnoLetivo novoAno = new AnoLetivo();
            novoAno.setAno(2025);
            novoAno.setDataInicio(java.time.LocalDate.of(2025, 2, 1));
            novoAno.setDataTermino(java.time.LocalDate.of(2025, 12, 15));

            AnoLetivoForm form = new AnoLetivoForm(anoLetivoService, novoAno, this::updateList);
            form.open();

            System.out.println("✅ Diálogo aberto com sucesso!");
        });

        Button voltarButton = new Button("Voltar ao Início", new Icon(VaadinIcon.ARROW_LEFT));
        voltarButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });

        HorizontalLayout toolbar = new HorizontalLayout(voltarButton, addButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.removeAllColumns();

        grid.addColumn(AnoLetivo::getAno).setHeader("Ano").setAutoWidth(true);

        // Formatar datas no padrão brasileiro
        grid.addColumn(ano -> {
            if (ano.getDataInicio() != null) {
                return String.format("%02d/%02d/%d",
                        ano.getDataInicio().getDayOfMonth(),
                        ano.getDataInicio().getMonthValue(),
                        ano.getDataInicio().getYear());
            }
            return "";
        }).setHeader("Início").setAutoWidth(true);

        grid.addColumn(ano -> {
            if (ano.getDataTermino() != null) {
                return String.format("%02d/%02d/%d",
                        ano.getDataTermino().getDayOfMonth(),
                        ano.getDataTermino().getMonthValue(),
                        ano.getDataTermino().getYear());
            }
            return "";
        }).setHeader("Término").setAutoWidth(true);

        grid.addColumn(ano -> ano.getStatus().getDescricao()).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(ano -> {
            Button edit = new Button(new Icon(VaadinIcon.EDIT));
            edit.addClickListener(e -> openForm(ano));
            return edit;
        }).setHeader("Ações").setAutoWidth(true);

        grid.getColumns().forEach(col -> col.setResizable(true).setSortable(true));
    }

    private void updateList() {
        grid.setItems(anoLetivoService.findAll());
    }

    private void openForm(AnoLetivo anoLetivo) {
        AnoLetivoForm form = new AnoLetivoForm(anoLetivoService, anoLetivo, this::updateList);
        form.open();
    }
}