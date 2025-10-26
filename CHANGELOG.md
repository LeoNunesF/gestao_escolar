# 📦 Changelog
Todas as mudanças relevantes neste projeto serão documentadas aqui.
Formato: [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) • Versionamento semântico.

---

## [0.3.2] - 2025-10-26
### Adicionado
- Atribuição de Professores às Turmas com papéis (Titular, Substituto, Coordenador).
- Coluna “Professores” na Gestão de Turmas exibindo nomes com o papel entre parênteses.
- Ação “Ver Turmas” na Gestão de Professores com listagem (Código, Nome, Papel).
- Botões “Imprimir” e “Fechar” no diálogo “Ver Turmas”.

### Corrigido
- LazyInitializationException ao listar turmas de um professor (uso de EntityGraph/DTO).
- Ajustes de UX em diálogos (tamanho, redimensionável).

### Alterado
- Atualização de README e ROADMAP.

---

## [0.3.1] - 2025-10-21
### Adicionado
- CRUD de turmas com geração automática de código
- Validação de campos com mensagens personalizadas
- Grid de visualização de turmas com colunas completas
- Separação de responsabilidades entre entidade e formulário
- Roadmap inicial

---

## [0.3.0] - 2025-10-15
### Adicionado
- CRUD de anos letivos
- Integração inicial de turmas com ano letivo

---

## [0.2.0] - 2025-10-10
### Adicionado
- Estrutura inicial com Spring Boot e Vaadin
- Cadastro e autenticação de usuários
- Perfis básicos (diretor, professor)