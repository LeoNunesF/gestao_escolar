package com.gestaoescolar.views.auth;

import com.gestaoescolar.service.auth.AuthService;
import com.vaadin.flow.component.Key;
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

        // Atalho Enter: atalho no botão é a solução mais simples e robusta
        loginButton.addClickShortcut(Key.ENTER);

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

        // foco inicial
        loginField.focus();
    }

    private void configurarCampos() {
        loginField.setWidthFull();
        loginField.setPlaceholder("Digite seu login");
        loginField.setRequired(true);

        senhaField.setWidthFull();
        senhaField.setPlaceholder("Digite sua senha");
        senhaField.setRequired(true);

        // Opcional: Enter no campo também pode submeter (fallback)
        // sem depender de string magic no key event
        loginField.addKeyDownListener(Key.ENTER, e -> realizarLogin());
        senhaField.addKeyDownListener(Key.ENTER, e -> realizarLogin());
    }

    private void realizarLogin() {
        if (loginField.isEmpty() || senhaField.isEmpty()) {
            Notification.show("Preencha login e senha", 2500, Notification.Position.MIDDLE);
            return;
        }

        try {
            boolean loginSucesso = authService.login(loginField.getValue(), senhaField.getValue());

            if (loginSucesso) {
                Notification.show("Login realizado com sucesso!", 1500, Notification.Position.BOTTOM_START);
                UI.getCurrent().navigate("");
            } else {
                Notification.show("Login ou senha incorretos", 3000, Notification.Position.MIDDLE);
                senhaField.clear();
                senhaField.focus();
            }
        } catch (Exception e) {
            Notification.show("Erro ao realizar login: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}