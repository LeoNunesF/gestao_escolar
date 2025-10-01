package com.gestaoescolar.views.shared;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

@Route(value = "perfil", layout = MainLayout.class)
@PageTitle("Meu Perfil | Gestão Escolar")
public class ProfileView extends VerticalLayout {

    public ProfileView() {
        setPadding(true);
        add(new H2("Meu Perfil"),
                new Paragraph("Esta funcionalidade estará disponível em breve."));
    }
}