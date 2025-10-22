package com.gestaoescolar.views.diretor;
import com.gestaoescolar.model.Professor;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.FormacaoAcademica;
import com.gestaoescolar.model.enums.Genero;
import com.gestaoescolar.service.escola.ProfessorService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import jakarta.persistence.*;

public class ProfessorForm extends FormLayout {

    private TextField nomeCompleto = new TextField("Nome Completo");
    private TextField cpf = new TextField("CPF");
    private TextField rg = new TextField("RG");
    private TextField email = new TextField("Email");
    private TextField telefone = new TextField("Telefone");
    private DatePicker dataNascimento = new DatePicker("Data de Nascimento");
    private ComboBox<Genero> genero = new ComboBox<>("Gênero");
    private ComboBox<FormacaoAcademica> formacao = new ComboBox<>("Formação Acadêmica");
    private TextField especializacao = new TextField("Especialização");
    private DatePicker dataAdmissao = new DatePicker("Data de Admissão");
    private Checkbox ativo = new Checkbox("Ativo");

    private Button salvar = new Button("Salvar");
    private Button cancelar = new Button("Cancelar");

    private Professor professor;
    private final ProfessorService professorService;
    private final Usuario usuarioLogado;

    public ProfessorForm(ProfessorService professorService, Usuario usuarioLogado) {
        this.professorService = professorService;
        this.usuarioLogado = usuarioLogado;

        genero.setItems(Genero.values());
        formacao.setItems(FormacaoAcademica.values());

        salvar.addClickListener(e -> fireEvent(new SaveEvent(this, professor)));
        cancelar.addClickListener(e -> fireEvent(new CloseEvent(this)));

        HorizontalLayout botoes = new HorizontalLayout(salvar, cancelar);
        add(nomeCompleto, cpf, rg, email, telefone, dataNascimento, genero, formacao,
                especializacao, dataAdmissao, ativo, botoes);
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
        if (professor != null) {
            nomeCompleto.setValue(professor.getNomeCompleto() != null ? professor.getNomeCompleto() : "");
            cpf.setValue(professor.getCpf() != null ? professor.getCpf() : "");
            rg.setValue(professor.getRg() != null ? professor.getRg() : "");
            email.setValue(professor.getEmail() != null ? professor.getEmail() : "");
            telefone.setValue(professor.getTelefone() != null ? professor.getTelefone() : "");
            dataNascimento.setValue(professor.getDataNascimento());
            genero.setValue(professor.getGenero());
            formacao.setValue(professor.getFormacao());
            especializacao.setValue(professor.getEspecializacao() != null ? professor.getEspecializacao() : "");
            dataAdmissao.setValue(professor.getDataAdmissao());
            ativo.setValue(professor.isAtivo());
        } else {
            nomeCompleto.clear();
            cpf.clear();
            rg.clear();
            email.clear();
            telefone.clear();
            dataNascimento.clear();
            genero.clear();
            formacao.clear();
            especializacao.clear();
            dataAdmissao.clear();
            ativo.clear();
        }
    }

    public static abstract class ProfessorFormEvent extends ComponentEvent<ProfessorForm> {
        private final Professor professor;

        protected ProfessorFormEvent(ProfessorForm source, Professor professor) {
            super(source, false);
            this.professor = professor;
        }

        public Professor getProfessor() {
            return professor;
        }
    }

    public static class SaveEvent extends ProfessorFormEvent {
        SaveEvent(ProfessorForm source, Professor professor) {
            super(source, professor);
        }
    }

    public static class CloseEvent extends ProfessorFormEvent {
        CloseEvent(ProfessorForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
