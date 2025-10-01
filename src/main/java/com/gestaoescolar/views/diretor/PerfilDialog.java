package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.PerfilUsuario;
import com.gestaoescolar.service.auth.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Arrays;

public class PerfilDialog extends Dialog {

    private final Usuario usuario;
    private final UsuarioService usuarioService;
    private final Usuario usuarioLogado;
    private final Runnable refreshCallback;

    private final ComboBox<PerfilUsuario> perfilCombo = new ComboBox<>("Novo Perfil");

    public PerfilDialog(Usuario usuario, UsuarioService usuarioService,
                        Usuario usuarioLogado, Runnable refreshCallback) {
        this.usuario = usuario;
        this.usuarioService = usuarioService;
        this.usuarioLogado = usuarioLogado;
        this.refreshCallback = refreshCallback;

        setHeaderTitle("Alterar Perfil de Usuário");
        setWidth("400px");

        createContent();
        createButtons();
    }

    private void createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Paragraph info = new Paragraph(
                "Alterando perfil de: " + usuario.getNomeCompleto() +
                        " (" + usuario.getLogin() + ")"
        );

        perfilCombo.setItems(Arrays.asList(PerfilUsuario.values()));
        perfilCombo.setItemLabelGenerator(PerfilUsuario::getNome);
        perfilCombo.setValue(usuario.getPerfil());
        perfilCombo.setWidthFull();

        content.add(info, perfilCombo);
        add(content);
    }

    private void createButtons() {
        Button confirmarButton = new Button("Confirmar", e -> alterarPerfil());
        confirmarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarButton = new Button("Cancelar", e -> close());

        HorizontalLayout buttons = new HorizontalLayout(confirmarButton, cancelarButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        getFooter().add(buttons);
    }

    private void alterarPerfil() {
        try {
            usuarioService.alterarPerfil(usuario.getId(), perfilCombo.getValue(), usuarioLogado);
            refreshCallback.run();
            close();
        } catch (Exception e) {
            getUI().ifPresent(ui -> ui.getPage().executeJs(
                    "alert($0)", "Erro ao alterar perfil: " + e.getMessage()
            ));
        }
    }
}