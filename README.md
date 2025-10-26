# 🏫 Sistema de Gestão Escolar

Sistema completo de gestão escolar desenvolvido em Java com Spring Boot e Vaadin, adaptado para a realidade das escolas brasileiras.

## 🚀 Status do Projeto

**✅ FUNCIONALIDADES IMPLEMENTADAS:**
- [x] **Sistema de Autenticação** - Login com perfis (Diretor, Secretaria, Professor)
- [x] **Gestão de Anos Letivos** - CRUD completo com datas no padrão brasileiro
- [x] **Gestão de Usuários** - CRUD completo com validações de segurança
- [x] **Interface Responsiva** - Menu adaptativo por perfil de usuário
- [x] **Configuração Brasileira** - Datas, horários e calendário em português
- [x] **Gestão de Turmas** - CRUD de turmas com geração de código
- [x] **Módulo de Professores (parcialmente concluído)** - CRUD de Professores, validações (CPF/Telefone), formulário de endereço, DatePicker em pt-BR, filtros básicos e integração inicial com serviço

**🔄 EM DESENVOLVIMENTO / PENDÊNCIAS:**
- [ ] Vincular Professores às Turmas (atribuição / lotação / papel: titular/substituto)
- [ ] Matrícula de Alunos
- [ ] Diário de Classe
- [ ] Controle de Frequência
- [ ] Lançamento de Notas e Boletins
- [ ] Relatórios e Estatísticas

**📋 PRÓXIMAS ETAPAS IMEDIATAS (Professores)**
- Implementar associação Professores <-> Turmas (ver recomendação abaixo)
- Adicionar UI de atribuição de professor em TurmaForm e visualização de turmas vinculadas no ProfessorView
- Testes automatizados (unit e integração) para services de Professor/Turma
- Migrações DB (Flyway/Liquibase) se for necessário manter histórico de dados
- Pequenas melhorias UX: máscara em tempo real (CPF/Telefone), paginação/ordenação na grid, export CSV/PDF

## 🛠️ Tecnologias Utilizadas

- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security
- **Frontend:** Vaadin 24, HTML5, CSS3
- **Banco de Dados:** H2 (desenvolvimento), PostgreSQL (produção)
- **Ferramentas:** Maven, IntelliJ IDEA, Git/GitHub
- **Padrões:** MVC, Repository Pattern, Dependency Injection

## 📋 Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- Git
- IDE (IntelliJ IDEA recomendado)

## 🚀 Como Executar

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/LeoNunesF/gestao_escolar.git
   cd gestao_escolar