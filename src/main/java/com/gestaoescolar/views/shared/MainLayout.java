package com.gestaoescolar.views.shared;

import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.PerfilUsuario;
import com.gestaoescolar.views.diretor.*;
import com.gestaoescolar.service.auth.AuthService;
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

public class MainLayout extends AppLayout {

    private final AuthService authService;
    private Usuario usuarioLogado;

    public MainLayout(AuthService authService) {
        this.authService = authService;
        this.usuarioLogado = authService.getUsuarioLogado();

        criarHeader();
        criarDrawer();
    }

    private void criarHeader() {
        H1 logo = new H1("Sistema de Gestão Escolar");
        logo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        String nomeUsuario = usuarioLogado != null ? usuarioLogado.getNomeCompleto() : "Visitante";
        Span saudacao = new Span("Olá, " + nomeUsuario);
        saudacao.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo,
                saudacao
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void criarDrawer() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        tabs.add(criarTab(VaadinIcon.DASHBOARD, "Dashboard", DashboardView.class));

        if (usuarioLogado != null && usuarioLogado.isDiretor()) {
            tabs.add(criarTab(VaadinIcon.CALENDAR, "Anos Letivos", AnoLetivoView.class));
            tabs.add(criarTab(VaadinIcon.USER, "Gestão de Usuários", UsuariosView.class));
            tabs.add(criarTab(VaadinIcon.GROUP, "Turmas", TurmasView.class));
            tabs.add(criarTab(VaadinIcon.GROUP, "Professores", ProfessorView.class));
            tabs.add(criarTab(VaadinIcon.USER, "Alunos", AlunosView.class));
        }

        if (usuarioLogado != null) {
            tabs.add(criarTab(VaadinIcon.USER_CARD, "Meu Perfil", ProfileView.class));
            tabs.add(criarTabLogout());
        }

        addToDrawer(tabs);
    }

    private Tab criarTab(VaadinIcon icon, String titulo, Class<? extends Component> viewClass) {
        System.out.println("🔗 Criando tab: " + titulo + " -> " + viewClass.getSimpleName());

        Icon icone = icon.create();
        RouterLink link = new RouterLink();
        link.add(icone, new Span(titulo));
        link.setRoute(viewClass);
        link.setTabIndex(-1);
        return new Tab(link);
        //tabs.add(criarTab(VaadinIcon.GROUP, "Turmas", TurmasView.class));
    }



    private Tab criarTabLogout() {
        Icon icone = VaadinIcon.SIGN_OUT.create();
        Span texto = new Span("Sair");

        HorizontalLayout content = new HorizontalLayout(icone, texto);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);

        content.addClickListener(event -> {
            authService.logout();
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        content.getStyle().set("cursor", "pointer")
                .set("padding", "var(--lumo-space-s)");

        return new Tab(content);
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
}