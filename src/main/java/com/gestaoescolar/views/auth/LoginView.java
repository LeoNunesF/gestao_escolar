package com.gestaoescolar.views.auth;

import com.gestaoescolar.service.auth.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | Gestão Escolar")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final AuthService authService;
    private final TextField loginField = new TextField("Login");
    private final PasswordField senhaField = new PasswordField("Senha");

    public LoginView(AuthService authService) {
        this.authService = authService;

        configurarLayout();
        criarFormularioLogin();

        // Redirecionar se já estiver logado
        if (authService.isUsuarioLogado()) {
            UI.getCurrent().navigate("");
        }
    }

    private void configurarLayout() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
    }

    private void criarFormularioLogin() {
        // Cabeçalho
        H1 titulo = new H1("Sistema de Gestão Escolar");
        titulo.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin-bottom", "2rem");

        Paragraph subtitulo = new Paragraph("Faça login para acessar o sistema");
        subtitulo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "2rem");

        // Campos do formulário
        configurarCampos();

        // Botão de login
        Button loginButton = new Button("Entrar", event -> realizarLogin());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();

        // Layout do formulário
        VerticalLayout formulario = new VerticalLayout();
        formulario.setWidth("400px");
        formulario.setPadding(true);
        formulario.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        formulario.add(loginField, senhaField, loginButton);

        add(titulo, subtitulo, formulario);
    }

    private void configurarCampos() {
        loginField.setWidthFull();
        loginField.setPlaceholder("Digite seu login");
        loginField.setRequired(true);

        senhaField.setWidthFull();
        senhaField.setPlaceholder("Digite sua senha");
        senhaField.setRequired(true);

        // Enter para submeter
        loginField.addKeyPressListener(e -> {
            if ("Enter".equals(e.getKey().getKeys().toString())) {
                realizarLogin();
            }
        });

        senhaField.addKeyPressListener(e -> {
            if ("Enter".equals(e.getKey().getKeys().toString())) {
                realizarLogin();
            }
        });
    }

    private void realizarLogin() {
        if (loginField.isEmpty() || senhaField.isEmpty()) {
            Notification.show("Preencha login e senha");
            return;
        }

        try {
            boolean loginSucesso = authService.login(loginField.getValue(), senhaField.getValue());

            if (loginSucesso) {
                Notification.show("Login realizado com sucesso!");
                UI.getCurrent().navigate("");
            } else {
                Notification.show("Login ou senha incorretos");
                senhaField.clear();
            }
        } catch (Exception e) {
            Notification.show("Erro ao realizar login: " + e.getMessage());
        }
    }
}