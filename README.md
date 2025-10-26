# 🏫 Sistema de Gestão Escolar

Sistema de gestão escolar desenvolvido em Java com Spring Boot e Vaadin, voltado à realidade das escolas brasileiras.

## 🚀 Status do Projeto

✅ Implementado
- Autenticação e perfis (Diretor, Secretaria, Professor)
- CRUD de Anos Letivos
- CRUD de Turmas com geração automática de código
- Visualização de Turmas (grid completo com série, nível, turno, ano, vagas, status)
- CRUD de Professores (dados pessoais, formação, endereço)
- Atribuição de Professores às Turmas com papéis:
    - Titular, Substituto, Coordenador
    - Diálogo de atribuição na Gestão de Turmas
    - Exibição dos vínculos na Gestão de Turmas (coluna “Professores”)
    - “Ver Turmas” na Gestão de Professores, com Código, Nome e Papel
- Ajustes de lazy-loading e UX (diálogos dimensionáveis, botões Imprimir/Fechar)

🔄 Em desenvolvimento
- Disciplinas e Matriz Curricular (modelagem e UI)
- Matrícula de alunos
- Diário de classe, frequência e avaliações
- Relatórios e exportações (PDF/CSV)
- Regras e permissões refinadas (Secretaria/Diretoria por caso de uso)

📋 Próximas etapas imediatas
- Disciplinas (entidade) e Matriz Curricular (por Ano Letivo + Nível + Série)
    - Evoluir o vínculo Professor↔Turma para incluir Disciplina como relação (hoje é texto)
- Regras:
    - Garantir 1 Titular por Turma
    - Bloquear atribuição de professor inativo/demitido
    - Validar períodos (início/fim) e sobreposições
- Permissões:
    - Validar perfis: Diretoria e Secretaria podem atribuir professores; Professor não
- Documentação e testes
    - Atualizar Roadmap/README (este arquivo)
    - Testes de serviço de atribuição e de UI

## 🛠️ Tecnologias

- Backend: Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security
- Frontend: Vaadin 24
- Banco: H2 (desenvolvimento); PostgreSQL (produção – recomendado)
- Ferramentas: Maven, Git/GitHub

## 📦 Executando localmente

```bash
git clone https://github.com/LeoNunesF/gestao_escolar.git
cd gestao_escolar
mvn spring-boot:run
# Acesse http://localhost:8080
```

## 🧱 Decisões de Modelagem (resumo)

- Professor↔Turma: entidade de junção (ProfessorTurma) com papel (Titular/Substituto/Coordenador), disciplina (hoje string), e período (início/fim).
- Disciplinas: entidade própria (não enum), para permitir:
    - Inclusão/remoção sem recompilar
    - Variação por etapa/série/rede
    - Compatibilidade com BNCC e matrizes por Ano Letivo
- Matriz Curricular: agrupa as Disciplinas por Ano Letivo + Nível + Série.
- Evolução incremental:
    - Manter campo “disciplina” em ProfessorTurma (string) por ora
    - Futuro: relacionar ProfessorTurma → Disciplina e oferecer seleção controlada pela Matriz da turma

## ✅ Regras e Validações (alvos)

- Professor↔Turma:
    - 1 Titular por turma
    - Não permitir vínculo com professor inativo/demitido
    - Validar períodos (início ≤ fim) e sobreposições
- Turmas:
    - Capacidade/vagas consistente com matrículas
    - Troca de titular com confirmação
- Segurança:
    - Perfis e autorização por caso de uso
    - Auditoria de alterações sensíveis

Contribuições e sugestões são bem-vindas!