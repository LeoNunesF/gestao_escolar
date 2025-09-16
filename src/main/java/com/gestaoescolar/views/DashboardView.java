package com.gestaoescolar.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainView.class)  // Rota principal
@PageTitle("Dashboard | Gestão Escolar")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        // Título principal
        H1 titulo = new H1("Sistema de Gestão Escolar");
        titulo.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin-bottom", "2rem");

        // Subtítulo
        H2 subtitulo = new H2("Bem-vindo ao Sistema de Gestão");
        subtitulo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "1.5rem");

        // Descrição
        Paragraph descricao = new Paragraph(
                "Este sistema permite gerenciar alunos, professores, turmas, notas e frequência. " +
                        "Use o menu lateral para navegar entre as funcionalidades."
        );
        descricao.getStyle()
                .set("text-align", "center")
                .set("max-width", "600px");

        // Estatísticas (exemplo)
        VerticalLayout estatisticas = new VerticalLayout();
        estatisticas.setAlignItems(Alignment.CENTER);
        estatisticas.add(
                new Paragraph("📊 150 Alunos cadastrados"),
                new Paragraph("👨‍🏫 25 Professores"),
                new Paragraph("🏫 15 Turmas ativas"),
                new Paragraph("📚 20 Disciplinas")
        );
        estatisticas.getStyle()
                .set("margin-top", "2rem")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "1.5rem")
                .set("border-radius", "var(--lumo-border-radius)");

        add(titulo, subtitulo, descricao, estatisticas);
    }
}