
```markdown name=CHANGELOG.md url=https://github.com/LeoNunesF/gestao_escolar/blob/a3033d3e27288c9ea8c632c768de375cc93ddcfc/CHANGELOG.md
# 📦 Changelog

Todas as mudanças relevantes neste projeto serão documentadas aqui.

O formato segue o padrão [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/), e este projeto utiliza versionamento semântico.

---

## [0.3.1] - 2025-10-26
### Adicionado
- CRUD de Professores (formulário e listagem).
- Validação de CPF com dígitos verificadores e máscara display (campo mostra formatado; valor salvo apenas com dígitos).
- Validação e formatação de Telefone (padrão brasileiro).
- Componente reutilizável EnderecoForm (views/components) com formatação de CEP.
- Enum Estado (UF) e integração no Endereco/EnderecoForm.
- Integração com ViaCEP (consulta assíncrona no blur do CEP) para autocomplete de logradouro, bairro, cidade e UF.
- DatePicker configurado para pt-BR (i18n + placeholder) em formulários relevantes.
- Filtros básicos na tela de Professores (por nome/CPF, status e formação).
- Melhorias na UX do login (enter ativa o botão e foco inicial).
- Atualizações no README e roadmap de features.

### Corrigido
- Correção da validação do campo RG no ProfessorForm (binder atualiza corretamente o bean antes do persist).
- Garantia de normalização do CPF antes do persist para satisfazer Bean Validation.

---

## [0.3.0] - 2025-10-21
### Adicionado
- CRUD de turmas com geração automática de código
- Validação de campos com mensagens personalizadas
- Grid de visualização de turmas com colunas completas
- Separação de responsabilidades entre entidade e formulário
- Atualização do README e criação do roadmap

---

## [0.2.0] - 2025-10-15
### Adicionado
- CRUD de anos letivos
- Validação de ano único por turma
- Integração com entidade Turma

---

## [0.1.0] - 2025-10-10
### Adicionado
- Estrutura inicial do projeto com Spring Boot e Vaadin
- Cadastro e autenticação de usuários
- Controle de perfis (diretor, professor)
- Interface de login e navegação básica

---

## 📌 Em breve
- Matrícula de alunos
- Atribuição de professores às turmas (próxima etapa recomendada)
- Boletins e avaliações
