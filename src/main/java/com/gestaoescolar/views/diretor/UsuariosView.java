package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.PerfilUsuario;
import com.gestaoescolar.service.auth.AuthService;
import com.gestaoescolar.service.auth.UsuarioService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;

@Route(value = "diretor/usuarios", layout = MainLayout.class)
@PageTitle("Gestão de Usuários | Gestão Escolar")
public class UsuariosView extends VerticalLayout {

    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final Usuario usuarioLogado;

    private final Grid<Usuario> grid = new Grid<>(Usuario.class);
    private final TextField filterText = new TextField();
    private final ComboBox<PerfilUsuario> filterPerfil = new ComboBox<>();
    private final ComboBox<Boolean> filterAtivo = new ComboBox<>();

    public UsuariosView(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.usuarioLogado = authService.getUsuarioLogado();

        setSizeFull();
        setPadding(true);

        add(new H2("Gestão de Usuários"));
        add(createToolbar());
        configureGrid();
        add(grid);

        updateList();
    }

    private HorizontalLayout createToolbar() {
        // Campo de filtro por nome/login
        filterText.setPlaceholder("Filtrar por nome ou login...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        // Filtro por perfil
        filterPerfil.setPlaceholder("Todos os perfis");
        filterPerfil.setItems(Arrays.asList(PerfilUsuario.values()));
        filterPerfil.setItemLabelGenerator(PerfilUsuario::getNome);
        filterPerfil.addValueChangeListener(e -> updateList());

        // Filtro por status
        filterAtivo.setPlaceholder("Todos os status");
        filterAtivo.setItems(true, false);
        filterAtivo.setItemLabelGenerator(ativo -> ativo ? "Ativos" : "Inativos");
        filterAtivo.addValueChangeListener(e -> updateList());

        // Botão novo usuário
        Button addButton = new Button("Novo Usuário", new Icon(VaadinIcon.PLUS));
        addButton.addClickListener(e -> openForm(new Usuario()));

        HorizontalLayout filters = new HorizontalLayout(filterText, filterPerfil, filterAtivo);
        filters.setAlignItems(Alignment.END);
        filters.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout(filters, addButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(Alignment.END);

        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.removeAllColumns();

        // Colunas principais
        grid.addColumn(Usuario::getLogin).setHeader("Login").setAutoWidth(true).setSortable(true);
        grid.addColumn(Usuario::getNomeCompleto).setHeader("Nome Completo").setAutoWidth(true).setSortable(true);
        grid.addColumn(Usuario::getEmail).setHeader("Email").setAutoWidth(true).setSortable(true);

        // Coluna de perfil com badge
        grid.addColumn(usuario -> usuario.getPerfil().getNome())
                .setHeader("Perfil")
                .setAutoWidth(true)
                .setSortable(true);

        // Coluna de status com ícone
        grid.addComponentColumn(usuario -> {
            Icon statusIcon = usuario.isAtivo() ?
                    new Icon(VaadinIcon.CHECK) : new Icon(VaadinIcon.CLOSE);
            statusIcon.setColor(usuario.isAtivo() ? "green" : "red");
            return statusIcon;
        }).setHeader("Ativo").setAutoWidth(true);

        // Coluna de data de criação
        grid.addColumn(Usuario::getDataCriacaoFormatada)
                .setHeader("Data Criação")
                .setAutoWidth(true)
                .setSortable(true);

        // Coluna de ações
        grid.addComponentColumn(usuario -> createActionButtons(usuario))
                .setHeader("Ações")
                .setAutoWidth(true);

        // Configurações da grid
        grid.getColumns().forEach(col -> col.setResizable(true));
        grid.setSelectionMode(Grid.SelectionMode.NONE);
    }

    private HorizontalLayout createActionButtons(Usuario usuario) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        // Botão editar
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addClickListener(e -> openForm(usuario));
        editButton.setTooltipText("Editar usuário");

        // Botão ativar/desativar
        Button statusButton = new Button(usuario.isAtivo() ?
                new Icon(VaadinIcon.BAN) : new Icon(VaadinIcon.CHECK));
        statusButton.addClickListener(e -> toggleStatusUsuario(usuario));
        statusButton.setTooltipText(usuario.isAtivo() ? "Desativar usuário" : "Ativar usuário");

        // Botão alterar perfil (apenas para diretor)
        Button perfilButton = new Button(new Icon(VaadinIcon.USER_CARD));
        perfilButton.addClickListener(e -> openPerfilDialog(usuario));
        perfilButton.setTooltipText("Alterar perfil");

        layout.add(editButton, statusButton);

        // Apenas diretor pode alterar perfis e desativar usuários
        if (usuarioLogado.isDiretor() && !usuario.getId().equals(usuarioLogado.getId())) {
            layout.add(perfilButton);
        }

        return layout;
    }

    private void updateList() {
        // Filtragem será implementada no service posteriormente
        // Por enquanto, mostra todos os usuários
        grid.setItems(usuarioService.listarTodosUsuarios(usuarioLogado));
    }

    private void openForm(Usuario usuario) {
        UsuarioForm form = new UsuarioForm(usuarioService, usuarioLogado, usuario, this::updateList);
        form.open();
    }

    private void toggleStatusUsuario(Usuario usuario) {
        try {
            if (usuario.isAtivo()) {
                usuarioService.desativarUsuario(usuario.getId(), usuarioLogado);
            } else {
                usuarioService.reativarUsuario(usuario.getId(), usuarioLogado);
            }
            updateList();
        } catch (Exception e) {
            showError("Erro ao alterar status: " + e.getMessage());
        }
    }

    private void openPerfilDialog(Usuario usuario) {
        try {
            PerfilDialog dialog = new PerfilDialog(usuario, usuarioService, usuarioLogado, this::updateList);
            dialog.open();
        } catch (Exception e) {
            showError("Erro ao abrir diálogo: " + e.getMessage());
        }
    }

    private void showError(String mensagem) {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "alert($0)", mensagem
        ));
    }
}