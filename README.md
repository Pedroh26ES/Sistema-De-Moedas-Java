# Valoriza Ae

Sistema de moeda estudantil para reconhecimento academico. Professores enviam moedas por merito, alunos acompanham saldo e resgatam beneficios, e empresas parceiras validam cupons no atendimento.

## Visao Geral

O Valoriza Ae conecta tres perfis em um fluxo unico:

1. O professor reconhece boas entregas com moedas e justificativa.
2. O aluno acumula saldo, acompanha extrato e resgata vantagens.
3. A empresa parceira confirma o cupom para concluir o atendimento.

O sistema foi desenvolvido em Java com Quarkus no back-end e React com Vite no front-end. O banco usado em desenvolvimento e testes e H2 em memoria, com persistencia gerenciada por Hibernate ORM + Panache.

## Funcionalidades

### Aluno

- Cadastro com nome, email, senha, CPF, RG, endereco, instituicao e curso.
- Instituicoes e cursos sao pre-cadastrados; o aluno seleciona a instituicao e depois escolhe um curso daquela instituicao.
- Dashboard com saldo, moedas recebidas, cupons validados e jornada do aluno.
- Catalogo de vantagens com custo em moedas, imagem, descricao e instrucao de uso do cupom.
- Resgate de vantagem com geracao de cupom.
- Bloqueio de resgate duplicado da mesma vantagem.
- Extrato com filtros por periodo: hoje, semana, mes, ano e todos.
- Notificacoes registradas quando recebe moedas, resgata cupom, tem cupom validado ou tem cupom temporariamente desativado.

### Professor

- Professor pre-cadastrado pela instituicao parceira.
- Dados institucionais visiveis no painel: nome, CPF, departamento e instituicao.
- Credito semestral de 1.000 moedas.
- Envio de moedas para alunos com justificativa obrigatoria.
- Modelos rapidos de justificativa.
- Historico de envios e notificacoes com filtros por periodo.

### Empresa Parceira

- Cadastro de empresa parceira.
- Criacao e edicao de vantagens com titulo, descricao, foto, custo em moedas e status.
- Preview da vantagem antes de publicar.
- Catalogo parceiro com vantagens publicadas e pausadas.
- Botao de publicar/pausar:
  - Publicar deixa a vantagem visivel para novos resgates.
  - Pausar oculta a vantagem para novos resgates.
  - Cupons pendentes de uma vantagem pausada ficam temporariamente desativados.
  - Ao publicar novamente, os cupons pendentes voltam a poder ser validados.
- Botao de excluir:
  - Exclui apenas vantagens que ainda nao possuem cupom/resgate vinculado.
  - Se a vantagem ja gerou cupom, a exclusao e bloqueada para preservar historico, extrato e validacao.
- Validacao de cupons pelo codigo apresentado pelo aluno.
- Fila de cupons recentes com status pendente, validado ou desativado.

## Regras de Negocio

- Cada perfil acessa apenas o proprio painel.
- Aluno deve se cadastrar com CPF, RG, endereco, instituicao e curso.
- Curso precisa pertencer a instituicao selecionada.
- Professor pertence explicitamente a uma instituicao e possui CPF e departamento.
- Professor recebe cota semestral de 1.000 moedas.
- Envio de moedas exige saldo suficiente, aluno existente, valor positivo e justificativa.
- Resgate de vantagem desconta moedas do aluno e gera um codigo de cupom.
- A mesma vantagem nao pode ser resgatada duas vezes pelo mesmo aluno.
- Cupom pendente precisa ser validado pela empresa parceira.
- Cupom validado fecha o atendimento e fica registrado no extrato.
- Cupom de vantagem pausada fica temporariamente desativado e nao pode ser validado ate a vantagem ser publicada novamente.
- Vantagem com cupom/resgate vinculado nao pode ser excluida, apenas pausada.
- Todas as notificacoes ficam registradas no sistema.

## Tecnologias

- Java 17
- Quarkus 3.15
- Maven
- React
- Vite
- Hibernate ORM com Panache
- H2 em memoria
- Qute como shell HTML para carregar a SPA React
- Lucide React para icones

## Como Rodar no VS Code

Abra o terminal na pasta:

```powershell
cd C:\Users\Pichau\Desktop\Sistema-De-Moedas\Código
```

Instale as dependencias do front-end:

```powershell
npm install
```

Gere o front-end React:

```powershell
npm run build:frontend
```

Rode o servidor Quarkus:

```powershell
mvn quarkus:dev
```

Depois acesse:

```text
http://localhost:8080
```

## Usuarios de Demonstracao

| Perfil | Email | Senha |
| --- | --- | --- |
| Aluno | aluno@moedas.com | 123456 |
| Professor | professor@moedas.com | 123456 |
| Empresa | empresa@moedas.com | 123456 |

## Comandos Uteis

Compilar o front-end:

```powershell
npm run build:frontend
```

Rodar testes:

```powershell
mvn test
```

Gerar pacote da aplicacao:

```powershell
mvn package
```

Rodar sem testes:

```powershell
mvn -DskipTests compile
```

## Estrutura do Projeto

```text
Código
├── frontend
│   └── src
│       ├── main.jsx       # SPA React
│       └── styles.css     # estilos da interface
├── src
│   ├── main
│   │   ├── java/br/com/sistemamoedas
│   │   │   ├── app         # dados iniciais
│   │   │   ├── controller  # rotas web e API REST
│   │   │   ├── domain      # entidades JPA
│   │   │   ├── repository  # repositories Panache
│   │   │   ├── security    # sessao, login e senha
│   │   │   └── service     # regras de negocio
│   │   └── resources
│   │       ├── META-INF/resources/react  # bundle React gerado
│   │       ├── templates                 # shells Qute
│   │       └── application.properties
│   └── test
│       └── java/br/com/sistemamoedas
└── docs
    ├── historias-usuario-expandidas.md
    └── diagrama-er-acesso-dados.md
```

## Modelagem e Persistencia

O projeto usa ORM com Jakarta Persistence/JPA, Hibernate ORM e Panache.

As principais entidades sao:

- `Usuario`
- `Aluno`
- `Professor`
- `EmpresaParceira`
- `Instituicao`
- `Curso`
- `Vantagem`
- `Transacao`
- `EmailNotificacao`

Os repositories funcionam como camada DAO, centralizando consultas e operacoes de persistencia.

Documentacao complementar:

- `docs/historias-usuario-expandidas.md`
- `docs/diagrama-er-acesso-dados.md`

## Banco de Dados

O projeto esta configurado para H2 em memoria:

```properties
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:valoriza-ae;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
quarkus.hibernate-orm.database.generation=update
```

Nos testes, o schema e recriado automaticamente:

```properties
%test.quarkus.hibernate-orm.database.generation=drop-and-create
```

## Validacao Atual

Ultima validacao feita:

```powershell
npm run build:frontend
mvn test
```

Resultado esperado dos testes:

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
```

## Observacoes

- O sistema usa dados iniciais em `DadosIniciais.java` para criar instituicoes, cursos, usuarios demo, empresas e vantagens.
- Emails sao registrados como notificacoes no banco, simulando uma caixa de saida.
- Para ver alteracoes do front-end, rode `npm run build:frontend` antes de abrir ou recarregar o sistema.
