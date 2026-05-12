<<<<<<< HEAD
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
=======
<a href="[https://classroom.github.com/online_ide?assignment_repo_id=99999999&assignment_repo_type=AssignmentRepo](https://classroom.github.com/online_ide?assignment_repo_id=99999999&assignment_repo_type=AssignmentRepo)"><img src="[https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)" width="200"/></a> <a href="[https://classroom.github.com/open-in-codespaces?assignment_repo_id=99999999](https://classroom.github.com/open-in-codespaces?assignment_repo_id=99999999)"><img src="[https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)" width="250"/></a>

---

# 🏷️ Sistema de Moeda Estudantil (Release 1) 🎓

> [!NOTE]
> Uma plataforma desenvolvida para estimular o reconhecimento do mérito estudantil através de uma moeda virtual, conectando alunos, professores e empresas parceiras da instituição de ensino.

<table>
  <tr>
    <td width="800px">
      <div align="justify">
        Este <b>README.md</b> apresenta a documentação do <b>Sistema de Moeda Estudantil</b>, desenvolvido como parte da disciplina de Laboratório de Desenvolvimento de Software. O projeto visa criar um ecossistema onde <i>professores</i> podem recompensar o bom desempenho de seus <i>alunos</i> enviando moedas virtuais. Os alunos, por sua vez, podem trocar essas moedas por produtos e descontos oferecidos por <i>empresas parceiras</i> cadastradas na plataforma. O sistema foi projetado utilizando a <b>arquitetura MVC</b>, integrando um modelo relacional (ER) sólido com estratégias de persistência (ORM/DAO) e garantindo uma experiência de usuário fluida e segura com autenticação para todos os atores envolvidos.
      </div>
    </td>
    <td>
      <div>
        <img src="https://joaopauloaramuni.github.io/image/logo_ES_vertical.png" alt="Logo do Projeto" width="120px"/>
      </div>
    </td>
  </tr> 
</table>

---

## 🚧 Status do Projeto

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/joaopauloaramuni/joaopauloaramuni/main.yml?branch=main)](#)
[![Test Coverage](https://codecov.io/gh/joaopauloaramuni/laboratorio-de-desenvolvimento-de-software/branch/main/graph/badge.svg)](#)
[![Versão](https://img.shields.io/badge/Release-1.0-blue)](#)
[![Licença](https://img.shields.io/badge/License-MIT-blue)](#licença)

---

## 📚 Índice
- [Links Úteis](#-links-úteis)
- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades Principais](#-funcionalidades-principais)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura e Modelagem](#-arquitetura-e-modelagem)
- [Instalação e Execução](#-instalação-e-execução)
- [Deploy](#-deploy)
- [Estrutura de Pastas](#-estrutura-de-pastas)
- [Demonstração](#-demonstração)
- [Testes](#-testes)
- [Documentações utilizadas](#-documentações-utilizadas)
- [Autores](#-autores)
- [Contribuição](#-contribuição)
- [Agradecimentos](#-agradecimentos)
- [Licença](#-licença)

---

## 🔗 Links Úteis
* 🌐 **Demo Online:** [Acesse a Aplicação Web](#)
  > 💻 **Descrição:** Link para a aplicação em ambiente de produção (Frontend).
* 📖 **Documentação da API:** [Swagger UI](#)
  > 📚 **Descrição:** Documentação dos endpoints RESTful do back-end.

---

## 📝 Sobre o Projeto
O **Sistema de Moeda Estudantil** foi concebido para resolver a necessidade de engajar e recompensar o mérito acadêmico de forma tangível. Muitas vezes, o bom comportamento e a participação em sala de aula não são refletidos apenas nas notas. 

Este sistema cria uma economia virtual controlada, onde:
- A **Instituição de Ensino** fornece a base de professores cadastrados.
- Os **Professores** recebem um saldo semestral de 1.000 moedas (acumulativo) para distribuir aos alunos mediante justificativa.
- Os **Alunos** acumulam essas moedas e consultam seus extratos.
- As **Empresas Parceiras** oferecem vantagens reais (descontos, materiais, refeições) que os alunos resgatam usando as moedas.

O sistema também cuida da emissão de cupons via e-mail, garantindo a segurança na troca presencial entre o aluno e a empresa parceira.

---

## ✨ Funcionalidades Principais

- 🔐 **Autenticação de Usuários:** Login seguro para Alunos, Professores e Empresas Parceiras.
- 🎓 **Portal do Aluno:** - Cadastro na plataforma (com vínculo à instituição).
  - Consulta de saldo e extrato detalhado.
  - Vitrine de vantagens e resgate de produtos/descontos.
- 👨‍🏫 **Portal do Professor:**
  - Consulta de saldo semestral (acumulativo).
  - Envio de moedas para alunos com campo de justificativa obrigatório.
- 🏢 **Portal da Empresa Parceira:**
  - Cadastro de perfil empresarial.
  - Gerenciamento de vantagens (inclusão de foto, descrição e custo em moedas).
- 📨 **Sistema de Notificações (E-mail):** - Notificação de recebimento de moedas para alunos.
  - Geração e envio de códigos de cupom simultâneos para aluno e parceiro na confirmação de um resgate.

---

## 🛠 Tecnologias Utilizadas

### 💻 Front-end
* **Biblioteca:** React
* **Build Tool:** Vite
* **Estilização:** Tailwind CSS ou Bootstrap (Definir conforme seu grupo)
* **Roteamento:** React Router DOM

### 🖥️ Back-end
* **Linguagem:** Java 17 (ou superior)
* **Framework:** Quarkus
* **Banco de Dados:** PostgreSQL / MySQL
* **Camada de Persistência:** Hibernate ORM com Panache
* **Envio de E-mails:** Quarkus Mailer

### ⚙️ Arquitetura e Engenharia
* **Padrão Arquitetural:** MVC (Model-View-Controller)
* **Modelagem UML:** PlantUML / Astah (Casos de Uso, Classes, Componentes)
* **Controle de Versão:** Git & GitHub

---

## 🏗 Arquitetura e Modelagem

O sistema foi estruturado com base no **padrão arquitetural MVC (Model-View-Controller)**, separando as regras de negócio (Model), a interface com o usuário (View) e o roteamento/orquestração (Controller).

O processo de desenvolvimento seguiu as especificações do laboratório:
1. **Lab03S01:** Modelagem inicial com Diagramas de Casos de Uso, Histórias do Usuário, Classes e Componentes.
2. **Lab03S02:** Criação do Modelo ER e implementação da persistência (ORM/DAO), focando nos CRUDs de aluno e parceiros.
3. **Lab03S03:** Consolidação do back-end, front-end e apresentação da release 1.

### Diagrama de Casos de Uso (Lab03S01)

Abaixo, a representação estrutural das interações dos atores com o sistema:

<div align="center">
  <img src="[http://www.plantuml.com/plantuml/png/jLHDRzim4BtxLupYIfO8m-aK2L8wA0hL2Q2O4e86a1wYwWwR3yI8K2A8GXXtZtC2hQnJjQfHn-iT6xN3Y2g_E-Qv_c_y-y9-Xb0eFj1zM6P9pXkI7P2I_S5O_XyB9qI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9](http://www.plantuml.com/plantuml/png/jLHDRzim4BtxLupYIfO8m-aK2L8wA0hL2Q2O4e86a1wYwWwR3yI8K2A8GXXtZtC2hQnJjQfHn-iT6xN3Y2g_E-Qv_c_y-y9-Xb0eFj1zM6P9pXkI7P2I_S5O_XyB9qI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9y9P9v_NqI_19XQY9P9)" alt="Diagrama de Casos de Uso"/>
</div>

*(Nota: Adicione a imagem PNG exportada do PlantUML na pasta `/assets` do repositório e altere o link do `src` acima)*

---

## 🔧 Instalação e Execução

### Pré-requisitos
* **Java 17+** (Necessário para o Quarkus).
* **Node.js** (Versão 18+ recomendada para o React/Vite).
* **PostgreSQL/MySQL** rodando localmente ou via Docker.

### 🔑 Variáveis de Ambiente
Crie ou edite os arquivos de configuração nas pastas correspondentes:

**Back-end Quarkus (`/backend/src/main/resources/application.properties`):**
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/moedaestudantil
quarkus.datasource.username=postgres
quarkus.datasource.password=suasenha
quarkus.hibernate-orm.database.generation=update
quarkus.mailer.from=seuemail@instituicao.edu.br
quarkus.mailer.host=smtp.gmail.com
quarkus.mailer.port=587
```

**Front-end React (`/frontend/.env.local`):**
```properties
VITE_API_URL=http://localhost:8080/api
```

### 📦 Instalação de Dependências

1. **Clone o Repositório:**
```bash
git clone https://github.com/Pedroh26ES/sistema-moeda-estudantil.git
cd sistema-moeda-estudantil
```

2. **Front-end:**
```bash
cd frontend
npm install
```

3. **Back-end:**
Como o Quarkus usa Maven, as dependências serão baixadas na primeira execução ou build.
```bash
cd backend
./mvnw clean install
```

### ⚡ Como Executar a Aplicação

Em terminais separados:

**Terminal 1 (Back-end - Modo Dev do Quarkus):**
```bash
cd backend
./mvnw compile quarkus:dev
```
*(A API iniciará na porta 8080)*

**Terminal 2 (Front-end - React/Vite):**
```bash
cd frontend
npm run dev
```
*(O Front-end iniciará na porta 5173 ou outra configurada no Vite)*

---

## 🚀 Deploy

Para apresentar na Sprint 03, a aplicação pode ser feita o deploy nos seguintes serviços gratuitos:
- **Front-end (React):** Vercel ou Netlify (build command: `npm run build`, output: `dist`).
- **Back-end (Quarkus):** O Quarkus pode ser compilado nativamente (GraalVM) ou em modo JVM. O deploy pode ser feito no Render, Railway ou Fly.io.
- **Banco de Dados:** Render PostgreSQL ou Supabase.

Certifique-se de configurar as variáveis de ambiente de produção nos respectivos painéis.

---

## 📂 Estrutura de Pastas

```text
📦 sistema-moeda-estudantil
├── 🌐 /frontend                 # Front-end da aplicação (React + Vite)
│   ├── 📁 public                # Arquivos estáticos e favicon
│   ├── 📁 src                   # Código-fonte do Front-end
│   │   ├── 🧩 /components       # Componentes visuais (Botões, Modais, Cards)
│   │   ├── 📄 /pages            # Telas da aplicação (Login, Dashboard, Cadastro)
│   │   ├── 🔌 /services         # Conexão com a API Backend (Axios/Fetch)
│   │   ├── 🎨 /styles           # Estilos globais e configurações CSS
│   │   └── ⚛️ App.jsx           # Componente raiz
│   ├── ⚙️ .env.local            # Variáveis de ambiente (URL da API)
│   └── 📦 package.json          # Dependências e scripts do Node.js
│
├── ☕ /backend                  # Back-end da API (Java Quarkus)
│   ├── 📁 src
│   │   ├── 📁 main/java         # Código-fonte Java
│   │   │   └── 📁 ...           # Estrutura de pacotes da sua equipe
│   │   │       ├── 🎮 /controllers  # Controladores REST (Endpoints)
│   │   │       ├── ⚙️ /services     # Regras de Negócio e Envio de E-mail
│   │   │       ├── 🗄️ /repositories # Padrão DAO / Acesso ao banco
│   │   │       └── 🧬 /models       # Entidades ORM (Hibernate/Panache)
│   │   └── 📁 main/resources    # Arquivos de configuração do sistema
│   │       └── 🔧 application.properties # Config. do BD e Mailer
│   └── 🐘 pom.xml               # Dependências e scripts do Maven
│
├── 📚 /docs                     # Documentações do Projeto
│   └── 📊 diagramas.png         # Arquivos de diagramas (Astah, PlantUML)
│
├── 📝 .gitignore                # Arquivos ignorados pelo Git
└── 📖 README.md                 # Documentação principal
```

---

## 🎥 Demonstração

| Tela | Captura de Tela |
| :---: | :---: |
| **Login do Sistema** | **Dashboard do Professor** |
| <img src="[https://joaopauloaramuni.github.io/image/aramunilogo.png](https://joaopauloaramuni.github.io/image/aramunilogo.png)" alt="Login" width="120px" height="120px"> | <img src="[https://joaopauloaramuni.github.io/image/aramunilogo.png](https://joaopauloaramuni.github.io/image/aramunilogo.png)" alt="Dashboard Professor" width="120px" height="120px"> |
| **Vitrine de Vantagens (Aluno)** | **Extrato de Moedas** |
| <img src="[https://joaopauloaramuni.github.io/image/aramunilogo.png](https://joaopauloaramuni.github.io/image/aramunilogo.png)" alt="Vantagens" width="120px" height="120px"> | <img src="[https://joaopauloaramuni.github.io/image/aramunilogo.png](https://joaopauloaramuni.github.io/image/aramunilogo.png)" alt="Extrato" width="120px" height="120px"> |

*(Substitua as imagens pelos prints reais da aplicação após o desenvolvimento do Front-end)*

---

## 🧪 Testes

Para testar a aplicação back-end:

```bash
# Executa os testes do Quarkus
cd backend
./mvnw test
```

---

## 🔗 Documentações utilizadas

* [**Quarkus IO** - Documentação Oficial](https://quarkus.io/guides/)
* [**PlantUML** - Documentação para Casos de Uso](https://plantuml.com/pt/use-case-diagram)
* [**React Router** - Navegação no Front-end](https://reactrouter.com/en/main)
* Guia da disciplina: Cronograma Lab. Desenv. Software.

---

## 👥 Autores

| 👤 Nome | :octocat: GitHub |
|---------|-----------------|
| Pedro Henrique | [@Pedroh26ES](https://github.com/Pedroh26ES) |
| Arthus Nunes | [@ArthurNGB](https://github.com/ArthurNGB) |
| Nayarisson Natãn | [@NatanMoroni08](https://github.com/NatanMoroni08) |


---

## 🤝 Contribuição

Este projeto é um trabalho acadêmico (Lab03). Se você for do grupo e desejar contribuir:
1. Faça o `git checkout -b feature/nome-da-feature`
2. Commit suas mudanças (`git commit -m 'feat: Cria CRUD de Empresa Parceira'`)
3. Dê push na branch (`git push origin feature/nome-da-feature`)
4. Abra um **Pull Request**.

---

## 🙏 Agradecimentos

* Ao **Prof. Dr. João Paulo Aramuni** pelas orientações nas Sprints, arquitetura MVC e padronização de projetos.
* À instituição por promover metodologias ágeis e projetos baseados em problemas reais no Laboratório de Desenvolvimento de Software.

---

## 📄 Licença

Este projeto é distribuído sob a **[Licença MIT](./LICENSE)**.
>>>>>>> 4314b5ef168a086dccbcec711ad434e631efb475
