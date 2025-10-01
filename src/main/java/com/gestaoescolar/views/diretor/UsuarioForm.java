package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.PerfilUsuario;
import com.gestaoescolar.service.auth.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;

import java.util.Arrays;

public class UsuarioForm extends Dialog {

    private final UsuarioService usuarioService;
    private final Usuario usuarioLogado;
    private final Runnable refreshCallback;

    // Campos do formulário
    private final TextField loginField = new TextField("Login");
    private final PasswordField senhaField = new PasswordField("Senha");
    private final PasswordField confirmarSenhaField = new PasswordField("Confirmar Senha");
    private final EmailField emailField = new EmailField("Email");
    private final TextField nomeCompletoField = new TextField("Nome Completo");
    private final ComboBox<PerfilUsuario> perfilField = new ComboBox<>("Perfil");

    private final Binder<Usuario> binder = new Binder<>(Usuario.class);

    public UsuarioForm(UsuarioService usuarioService, Usuario usuarioLogado,
                       Usuario usuario, Runnable refreshCallback) {
        this.usuarioService = usuarioService;
        this.usuarioLogado = usuarioLogado;
        this.refreshCallback = refreshCallback;

        setHeaderTitle(usuario.getId() == null ? "Novo Usuário" : "Editar Usuário");
        setWidth("600px");
        setModal(true);
        setCloseOnOutsideClick(false);

        configureForm();
        createFormLayout();
        createButtons();

        binder.setBean(usuario);

        // Configurações específicas para edição vs criação
        if (usuario.getId() != null) {
            configurarModoEdicao();
        }
    }

    private void configureForm() {
        // Configurar campos
        loginField.setRequired(true);
        loginField.setMinLength(3);
        loginField.setMaxLength(50);
        loginField.setPlaceholder("Digite o login de acesso");

        senhaField.setRequired(true);
        senhaField.setMinLength(6);
        senhaField.setPlaceholder("Mínimo 6 caracteres");
        senhaField.setRevealButtonVisible(true);

        confirmarSenhaField.setRequired(true);
        confirmarSenhaField.setPlaceholder("Digite a senha novamente");
        confirmarSenhaField.setRevealButtonVisible(true);

        emailField.setRequired(true);
        emailField.setPlaceholder("exemplo@escola.com");

        nomeCompletoField.setRequired(true);
        nomeCompletoField.setMinLength(5);
        nomeCompletoField.setMaxLength(100);

        perfilField.setItems(Arrays.asList(PerfilUsuario.values()));
        perfilField.setItemLabelGenerator(PerfilUsuario::getNome);
        perfilField.setRequired(true);

        // Configurar validações do binder
        binder.forField(loginField)
                .asRequired("Login é obrigatório")
                .withValidator(login -> login.length() >= 3, "Login deve ter pelo menos 3 caracteres")
                .withValidator(login -> !usuarioService.existeUsuarioComLogin(login) ||
                                (binder.getBean().getId() != null &&
                                        login.equals(binder.getBean().getLogin())),
                        "Já existe um usuário com este login")
                .bind(Usuario::getLogin, Usuario::setLogin);

        binder.forField(emailField)
                .asRequired("Email é obrigatório")
                .withValidator(new EmailValidator("Email inválido"))
                .withValidator(email -> !usuarioService.existeUsuarioComEmail(email) ||
                                (binder.getBean().getId() != null &&
                                        email.equals(binder.getBean().getEmail())),
                        "Já existe um usuário com este email")
                .bind(Usuario::getEmail, Usuario::setEmail);

        binder.forField(nomeCompletoField)
                .asRequired("Nome completo é obrigatório")
                .withValidator(nome -> nome.length() >= 5, "Nome deve ter pelo menos 5 caracteres")
                .bind(Usuario::getNomeCompleto, Usuario::setNomeCompleto);

        binder.forField(perfilField)
                .asRequired("Perfil é obrigatório")
                .bind(Usuario::getPerfil, Usuario::setPerfil);

        // Validação customizada para senhas
        binder.forField(senhaField)
                .withValidator(senha -> {
                    if (binder.getBean().getId() == null) {
                        // Novo usuário: senha obrigatória
                        return senha != null && senha.length() >= 6;
                    } else {
                        // Edição: senha opcional (manter atual se vazia)
                        return senha == null || senha.isEmpty() || senha.length() >= 6;
                    }
                }, "Senha deve ter pelo menos 6 caracteres")
                .bind(usuario -> "", (usuario, senha) -> {
                    // Só atualiza a senha se não estiver vazia
                    if (senha != null && !senha.trim().isEmpty()) {
                        usuario.setSenha(senha);
                    }
                });

        // Validação de confirmação de senha
        confirmarSenhaField.addValueChangeListener(e -> validarConfirmacaoSenha());
    }

    private void validarConfirmacaoSenha() {
        String senha = senhaField.getValue();
        String confirmacao = confirmarSenhaField.getValue();

        if (senha != null && confirmacao != null && !senha.equals(confirmacao)) {
            confirmarSenhaField.setErrorMessage("As senhas não coincidem");
            confirmarSenhaField.setInvalid(true);
        } else {
            confirmarSenhaField.setErrorMessage(null);
            confirmarSenhaField.setInvalid(false);
        }
    }

    private void configurarModoEdicao() {
        // Em modo edição, senha não é obrigatória (pode manter a atual)
        senhaField.setRequiredIndicatorVisible(false);
        senhaField.setPlaceholder("Deixe em branco para manter a senha atual");
        confirmarSenhaField.setRequiredIndicatorVisible(false);
        confirmarSenhaField.setPlaceholder("Deixe em branco para manter a senha atual");

        // Login não pode ser alterado em edição
        loginField.setReadOnly(true);
        loginField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    private void createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(
                loginField, senhaField, confirmarSenhaField,
                emailField, nomeCompletoField, perfilField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.setColspan(nomeCompletoField, 2);

        add(formLayout);
    }

    private void createButtons() {
        Button salvarButton = new Button("Salvar", e -> salvarUsuario());
        salvarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarButton = new Button("Cancelar", e -> close());

        HorizontalLayout buttons = new HorizontalLayout(salvarButton, cancelarButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        getFooter().add(buttons);
    }

    private void salvarUsuario() {
        // Validar confirmação de senha
        validarConfirmacaoSenha();
        if (confirmarSenhaField.isInvalid()) {
            Notification.show("Corrija os erros antes de salvar");
            return;
        }

        if (binder.validate().isOk()) {
            try {
                Usuario usuario = binder.getBean();

                if (usuario.getId() == null) {
                    // NOVO USUÁRIO
                    usuarioService.criarUsuario(usuario, usuarioLogado);
                    Notification.show("Usuário criado com sucesso!");
                } else {
                    // EDIÇÃO DE USUÁRIO
                    usuarioService.atualizarUsuario(usuario.getId(), usuario, usuarioLogado);
                    Notification.show("Usuário atualizado com sucesso!");
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
                Notification.show("Erro ao salvar usuário: " + e.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Corrija os erros antes de salvar");
        }
    }
}