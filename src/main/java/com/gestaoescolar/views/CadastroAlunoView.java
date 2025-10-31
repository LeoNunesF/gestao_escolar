package com.gestaoescolar.views;

import com.gestaoescolar.model.Aluno;
import com.gestaoescolar.service.escola.AlunoService;
import com.gestaoescolar.views.diretor.AlunosView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

//@Route(value = "cadastro-aluno", layout = MainView.class)
//@PageTitle("Cadastro de Aluno | Gestão Escolar")
public class CadastroAlunoView extends VerticalLayout {

    private final AlunoService alunoService;

    private TextField matricula = new TextField("Matrícula");
    private TextField nome = new TextField("Nome completo");
    private DatePicker dataNascimento = new DatePicker("Data de nascimento");
    private TextField turma = new TextField("Turma");
    private TextField responsavel = new TextField("Responsável");
    private TextField telefone = new TextField("Telefone");
    private TextField email = new TextField("Email");

    public CadastroAlunoView(AlunoService alunoService) {
        this.alunoService = alunoService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        add(
                new H2("Cadastro de Aluno"),
                criarFormulario()
        );
    }

    private FormLayout criarFormulario() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("600px");

        // Configurar campos
        matricula.setRequired(true);
        nome.setRequired(true);
        telefone.setRequired(true);

        // Adicionar campos ao formulário
        formLayout.add(
                matricula, nome, dataNascimento,
                turma, responsavel, telefone, email
        );

        // Botões
        Button salvar = new Button("Salvar", e -> salvarAluno());
        salvar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelar = new Button("Cancelar", e -> getUI().ifPresent(ui -> ui.navigate(AlunosView.class)));

        HorizontalLayout botoes = new HorizontalLayout(salvar, cancelar);
        formLayout.add(botoes);

        return formLayout;
    }

    private void salvarAluno() {
        if (matricula.isEmpty() || nome.isEmpty() || telefone.isEmpty()) {
            Notification.show("Preencha os campos obrigatórios!", 3000, Notification.Position.MIDDLE);
            return;
        }

       // Aluno aluno = new Aluno();
        //aluno.setMatricula(matricula.getValue());
       // aluno.setNome(nome.getValue());
       // aluno.setDataNascimento(dataNascimento.getValue());
       // aluno.setTurma(turma.getValue());
      //  aluno.setResponsavel(responsavel.getValue());
      //  aluno.setTelefone(telefone.getValue());
      //  aluno.setEmail(email.getValue());

      //  alunoService.save(aluno);

        Notification.show("Aluno salvo com sucesso!", 3000, Notification.Position.MIDDLE);
        getUI().ifPresent(ui -> ui.navigate(AlunosView.class));
    }
}