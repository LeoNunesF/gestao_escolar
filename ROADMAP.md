# 🗺️ Roadmap do Projeto: Sistema de Gestão Escolar

Este documento apresenta o plano de desenvolvimento do sistema de gestão escolar, com etapas concluídas, em andamento e futuras funcionalidades.

---

## ✅ Etapas concluídas

- [x] Estrutura inicial do projeto com Spring Boot e Vaadin
- [x] Cadastro e autenticação de usuários
- [x] Controle de perfis (diretor, professor)
- [x] CRUD de anos letivos
- [x] CRUD de turmas com geração automática de código
- [x] Interface de visualização de turmas com grid completo
- [x] Validação de campos e regras de negócio
- [x] CRUD de professores
- [x] Integração de professores com turmas (atribuição)
- [x] Exibição de vínculos com papéis (Titular/Substituto/Coordenador) nas views

---

## 🚧 Em andamento

- [ ] Disciplinas e Matriz Curricular (modelagem e UI)
- [ ] Atualização de README e documentação técnica
- [ ] Refatoração de views para melhor organização
- [ ] Testes manuais e automatizados do fluxo completo (usuário → turma → professor)

---

## 🧩 Próximas etapas

- [ ] CRUD de alunos
- [ ] Matrícula de alunos em turmas (com controle de vagas)
- [ ] Controle de presença e frequência
- [ ] Diários de classe e avaliações
- [ ] Boletins e relatórios pedagógicos
- [ ] Relatórios administrativos
- [ ] Dashboard com indicadores
- [ ] Exportação de dados (PDF, CSV/Excel)
- [ ] Controle de horários e salas
- [ ] Notificações internas e mensagens

---

## 📌 Observações

- Disciplinas serão entidade própria (não enum), permitindo ajustes por etapa/série e por ano letivo, com histórico e compatibilidade com BNCC.
- Perfis e permissões: Diretoria e Secretaria com poderes de gestão (atribuição de professores, etc.). Professores com permissões restritas ao pedagógico.
- O roadmap pode ser ajustado conforme o avanço do projeto e novas necessidades. Sugestões e contribuições são bem-vindas!