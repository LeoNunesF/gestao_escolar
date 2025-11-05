package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Disciplina;
import com.gestaoescolar.service.escola.CurriculumService;
import com.gestaoescolar.views.shared.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "diretor/disciplinas", layout = MainLayout.class)
@PageTitle("Disciplinas | Gestão Escolar")
public class DisciplinaView extends VerticalLayout {

    private final CurriculumService curriculumService;

    private final Grid<Disciplina> grid = new Grid<>(Disciplina.class, false);

    private final TextField codigo = new TextField("Código");
    private final TextField nome = new TextField("Nome");
    private final TextArea descricao = new TextArea("Descrição");
    private final IntegerField carga = new IntegerField("Carga horária");

    private Disciplina editing;

    public DisciplinaView(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
        setSizeFull();
        add(new H2("Disciplinas"));

        configureGrid();
        add(createForm(), grid);

        updateList();
    }

    private void configureGrid() {
        grid.addColumn(Disciplina::getCodigo).setHeader("Código").setAutoWidth(true);
        grid.addColumn(Disciplina::getNome).setHeader("Nome").setAutoWidth(true);
        grid.addColumn(d -> d.getCargaHoraria() != null ? d.getCargaHoraria().toString() : "").setHeader("Carga");
        grid.setItems(List.of());
        grid.asSingleSelect().addValueChangeListener(ev -> edit(ev.getValue()));
    }

    private FormLayout createForm() {
        FormLayout form = new FormLayout();
        form.add(codigo, nome, carga, descricao);

        Button salvar = new Button("Salvar", e -> onSave());
        Button novo = new Button("Novo", e -> {
            editing = new Disciplina();
            clearForm();
        });
        Button delete = new Button("Excluir", e -> {
            if (editing != null && editing.getId() != null) {
                try {
                    curriculumService.deleteDisciplina(editing.getId());
                    Notification.show("Disciplina excluída.", 2500, Notification.Position.BOTTOM_START);
                    updateList();
                    editing = null;
                    clearForm();
                } catch (Exception ex) {
                    Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            }
        });

        // NOVO: importa disciplinas padrão do enum sem duplicar
        Button padroesBR = new Button("Padrões BR", e -> {
            try {
                int criadas = curriculumService.importAllDefaultDisciplines();
                Notification.show(
                        criadas == 0 ? "Nenhuma nova disciplina padrão a importar."
                                : ("Importadas " + criadas + " disciplinas padrão."),
                        3000, Notification.Position.BOTTOM_START
                );
                updateList();
            } catch (Exception ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        HorizontalLayout actions = new HorizontalLayout(salvar, novo, delete, padroesBR);
        form.add(actions);
        return form;
    }

    private void edit(Disciplina d) {
        this.editing = d;
        if (d == null) {
            clearForm();
            return;
        }
        codigo.setValue(d.getCodigo() != null ? d.getCodigo() : "");
        nome.setValue(d.getNome() != null ? d.getNome() : "");
        descricao.setValue(d.getDescricao() != null ? d.getDescricao() : "");
        carga.setValue(d.getCargaHoraria());
    }

    private void clearForm() {
        codigo.clear();
        nome.clear();
        descricao.clear();
        carga.clear();
    }

    private void onSave() {
        try {
            if (editing == null) editing = new Disciplina();
            editing.setCodigo(codigo.getValue());
            editing.setNome(nome.getValue());
            editing.setDescricao(descricao.getValue());
            editing.setCargaHoraria(carga.getValue() != null ? carga.getValue() : null);

            curriculumService.createOrUpdateDisciplina(editing);
            Notification.show("Disciplina salva.", 2500, Notification.Position.BOTTOM_START);
            updateList();
        } catch (Exception ex) {
            Notification.show("Erro ao salvar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void updateList() {
        List<Disciplina> ds = curriculumService.listAllDisciplines();
        grid.setItems(ds);
    }
}