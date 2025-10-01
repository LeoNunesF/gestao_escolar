package com.gestaoescolar.views.shared;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Gestão Escolar")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        H1 titulo = new H1("Sistema de Gestão Escolar");
        titulo.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin-bottom", "2rem");

        H2 subtitulo = new H2("Bem-vindo ao Sistema de Gestão");
        subtitulo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "1.5rem");

        Paragraph descricao = new Paragraph(
                "Use o menu lateral para navegar entre as funcionalidades."
        );
        descricao.getStyle()
                .set("text-align", "center")
                .set("max-width", "600px");

        add(titulo, subtitulo, descricao);
    }
}