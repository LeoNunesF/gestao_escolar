package com.gestaoescolar.views.diretor;

import com.gestaoescolar.model.Disciplina;
import com.gestaoescolar.model.Turma;
import com.gestaoescolar.model.TurmaDisciplina;
import com.gestaoescolar.model.enums.DisciplinaPadrao;
import com.gestaoescolar.service.escola.CurriculumService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.*;
import java.util.stream.Collectors;

public class TurmaOfertaDisciplinasDialog extends Dialog {

    private final CurriculumService curriculumService;
    private final Turma turma;
    private final Runnable onChanged;

    private final Grid<Disciplina> grid = new Grid<>(Disciplina.class, false);
    private final TextField filtro = new TextField("Filtrar (código ou nome)");
    private final IntegerField cargaAplicada = new IntegerField("Carga Horária (aplicar às selecionadas) - opcional");

    private List<Disciplina> todasDisciplinas = List.of();
    private Set<Long> disciplinasJaAtribuidasIds = Set.of();

    private final Span info = new Span();

    public TurmaOfertaDisciplinasDialog(CurriculumService curriculumService,
                                        Turma turma,
                                        Runnable onChanged) {
        this.curriculumService = curriculumService;
        this.turma = turma;
        this.onChanged = onChanged;

        setHeaderTitle("Ofertar Disciplinas (lote) — " + turma.getCodigo() + " - " + turma.getNomeTurma());
        setWidth("980px");
        setHeight("75vh");
        setDraggable(true);
        setResizable(true);

        configureComponents();
        loadData();
    }

    private void configureComponents() {
        // Grid de disciplinas
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(Disciplina::getCodigo).setHeader("Código").setAutoWidth(true);
        grid.addColumn(Disciplina::getNome).setHeader("Nome").setAutoWidth(true);
        grid.addColumn(d -> disciplinasJaAtribuidasIds.contains(d.getId()) ? "Sim" : "Não")
                .setHeader("Já atribuída?").setAutoWidth(true);

        grid.addSelectionListener(ev -> updateInfo());

        // Filtro simples (cliente)
        filtro.setClearButtonVisible(true);
        filtro.addValueChangeListener(ev -> applyFilter());

        cargaAplicada.setMin(0);
        cargaAplicada.setStepButtonsVisible(true);

        Button selecionarPadroes = new Button("Selecionar Padrões BR", e -> onSelecionarPadroes());
        Button limparSelecao = new Button("Limpar seleção", e -> grid.deselectAll());
        Button adicionarSelecionadas = new Button("Adicionar selecionadas", e -> onAdicionarSelecionadas());
        Button fechar = new Button("Fechar", e -> close());

        HorizontalLayout actions = new HorizontalLayout(selecionarPadroes, limparSelecao, adicionarSelecionadas, fechar);

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.add(filtro, cargaAplicada);
        form.setColspan(filtro, 2);

        add(form, grid, info, actions);
    }

    private void loadData() {
        // Todas as disciplinas da escola
        todasDisciplinas = new ArrayList<>(curriculumService.listAllDisciplines());

        // Disciplinas já atribuídas à turma
        List<TurmaDisciplina> tds = curriculumService.listByTurma(turma.getId());
        disciplinasJaAtribuidasIds = tds.stream()
                .map(td -> td.getDisciplina() != null ? td.getDisciplina().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        grid.setItems(todasDisciplinas);
        updateInfo();
    }

    private void applyFilter() {
        String q = filtro.getValue() != null ? filtro.getValue().trim().toLowerCase() : "";
        if (q.isBlank()) {
            grid.setItems(todasDisciplinas);
        } else {
            List<Disciplina> filtradas = todasDisciplinas.stream()
                    .filter(d ->
                            (d.getCodigo() != null && d.getCodigo().toLowerCase().contains(q)) ||
                                    (d.getNome() != null && d.getNome().toLowerCase().contains(q))
                    )
                    .toList();
            grid.setItems(filtradas);
        }
        // manter seleção coerente mesmo após refiltrar
        updateInfo();
    }

    private void onSelecionarPadroes() {
        try {
            // Garante que padrões existem (importa faltantes)
            curriculumService.importAllDefaultDisciplines();

            // Mapa código->Disciplina (case-insensitive)
            Map<String, Disciplina> byCodigoCI = curriculumService.listAllDisciplines().stream()
                    .collect(Collectors.toMap(d -> d.getCodigo().toLowerCase(), d -> d, (a, b) -> a));

            // Seleciona padrões que não estão atribuídos
            int selecionadas = 0;
            for (DisciplinaPadrao dp : DisciplinaPadrao.values()) {
                Disciplina d = byCodigoCI.get(dp.getCodigo().toLowerCase());
                if (d != null) {
                    if (!disciplinasJaAtribuidasIds.contains(d.getId())) {
                        grid.select(d);
                        selecionadas++;
                    }
                }
            }
            if (selecionadas == 0) {
                Notification.show("Nenhuma disciplina padrão para selecionar (já atribuídas ou inexistentes).", 3000, Notification.Position.BOTTOM_START);
            }
            updateInfo();
        } catch (Exception ex) {
            Notification.show("Erro ao selecionar padrões: " + safeMsg(ex), 4000, Notification.Position.MIDDLE);
        }
    }

    private void onAdicionarSelecionadas() {
        Set<Disciplina> selecionadas = grid.getSelectedItems();
        if (selecionadas == null || selecionadas.isEmpty()) {
            Notification.show("Selecione ao menos uma disciplina.", 3000, Notification.Position.MIDDLE);
            return;
        }
        Integer carga = cargaAplicada.getValue() != null ? cargaAplicada.getValue() : null;

        int adicionadas = 0;
        int ignoradas = 0;
        List<String> ignoradasNomes = new ArrayList<>();

        for (Disciplina d : selecionadas) {
            // Evita tentativa em já atribuídas
            if (disciplinasJaAtribuidasIds.contains(d.getId())) {
                ignoradas++;
                ignoradasNomes.add(label(d));
                continue;
            }
            try {
                curriculumService.addDisciplinaToTurma(turma.getId(), d.getId(), carga);
                adicionadas++;
            } catch (Exception ex) {
                // Provável duplicidade, ignora e contabiliza
                ignoradas++;
                ignoradasNomes.add(label(d));
            }
        }

        String resumo = "Adicionadas: " + adicionadas + ". Ignoradas: " + ignoradas + (ignoradas > 0 ? " (" + String.join(", ", limit(ignoradasNomes, 5)) + (ignoradasNomes.size() > 5 ? ", ..." : "") + ")" : "") + ".";
        Notification.show(resumo, 4000, Notification.Position.BOTTOM_START);

        // Recarrega dados e mantém diálogo aberto
        loadData();
        grid.deselectAll();
        updateInfo();

        if (adicionadas > 0 && onChanged != null) {
            onChanged.run();
        }
    }

    private String label(Disciplina d) {
        String c = d.getCodigo() != null ? d.getCodigo() : "";
        String n = d.getNome() != null ? d.getNome() : "";
        return c + (n.isBlank() ? "" : " - " + n);
    }

    private List<String> limit(List<String> lista, int max) {
        if (lista.size() <= max) return lista;
        return lista.subList(0, max);
    }

    private void updateInfo() {
        Set<Disciplina> sel = grid.getSelectedItems();
        long jaAtribuidasNaSelecao = sel.stream().filter(d -> disciplinasJaAtribuidasIds.contains(d.getId())).count();
        info.setText("Selecionadas: " + sel.size() + " | Já atribuídas na turma: " + jaAtribuidasNaSelecao + " (serão ignoradas).");
    }

    private String safeMsg(Exception ex) {
        return ex.getMessage() != null ? ex.getMessage() : "Erro inesperado";
    }
}