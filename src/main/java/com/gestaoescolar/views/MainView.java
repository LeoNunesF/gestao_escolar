package com.gestaoescolar.views;

import com.gestaoescolar.views.shared.DashboardView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;

public class MainView extends AppLayout {

    public MainView() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Sistema de Gestão Escolar");
        logo.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        Tabs tabs = new Tabs(
                createTab(VaadinIcon.DASHBOARD, "Dashboard", DashboardView.class),
                createTab(VaadinIcon.USER, "Alunos", AlunosView.class),
                createTab(VaadinIcon.ACADEMY_CAP, "Professores", "#"),
                createTab(VaadinIcon.GROUP, "Turmas", "#"),
                createTab(VaadinIcon.BOOK, "Disciplinas", "#"),
                createTab(VaadinIcon.CLIPBOARD, "Notas", "#"),
                createTab(VaadinIcon.CALENDAR, "Frequência", "#"),
                createTab(VaadinIcon.CHART, "Relatórios", "#")
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);
    }

    private Tab createTab(VaadinIcon icon, String title, Class<? extends Component> viewClass) {
        Icon iconComponent = icon.create();
        RouterLink link = new RouterLink();
        link.add(iconComponent, new Span(title));
        link.setRoute(viewClass);
        link.setTabIndex(-1);

        return new Tab(link);
    }

    private Tab createTab(VaadinIcon icon, String title, String href) {
        Icon iconComponent = icon.create();
        Span span = new Span(title);
        return new Tab(iconComponent, span);
    }
}

