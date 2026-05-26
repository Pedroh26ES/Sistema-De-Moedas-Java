# Release 2 e Release 3 - diagramas e rastreabilidade

Este documento consolida os artefatos pedidos nas Releases 2 e 3 do sistema Valoriza Ae.

## Release 2 - requisitos cobertos

- Envio de moedas pelo professor com saldo suficiente e justificativa obrigatoria.
- Extrato do professor com envios e credito semestral.
- Extrato do aluno com recebimentos, resgates, cupons e status.
- Email de confirmacao para o aluno que recebeu moedas.
- Email de confirmacao para o professor que enviou moedas.
- Cadastro, edicao, pausa/publicacao e exclusao segura de vantagens pela empresa parceira.
- Listagem de vantagens para o aluno, com filtro por disponibilidade, adquiridas e metas.
- Troca de vantagem pelo aluno com desconto imediato do saldo.
- Bloqueio de resgate duplicado da mesma vantagem pelo mesmo aluno.

## Release 3 - requisitos cobertos

- Email de cupom para aluno e empresa parceira.
- Codigo de cupom unico gerado automaticamente pelo sistema.
- QR Code unico para cada cupom.
- QR Code escaneavel como URL de validacao: abre o painel da empresa com o cupom preenchido.
- Validacao do cupom pela empresa dona da vantagem.
- Fila RabbitMQ obrigatoria para eventos de envio de moedas, cupom gerado, cupom validado e mudancas de status.
- Configuracao de deploy em Render para o backend Quarkus.
- Configuracao de deploy em Vercel para o frontend React.

## Diagrama de sequencia - enviar moedas

```mermaid
sequenceDiagram
    actor Professor
    participant React as Frontend React
    participant API as ApiController
    participant Service as MoedaService
    participant DB as Banco via ORM
    participant Email as EmailOutbox
    participant MQ as RabbitMQ obrigatorio
    actor Aluno

    Professor->>React: informa aluno, valor e justificativa
    React->>API: POST /api/professor/envios
    API->>Service: enviarMoedas(professorId, alunoId, valor, motivo)
    Service->>DB: consulta professor e aluno
    Service->>Service: valida saldo, valor e motivo
    Service->>DB: debita professor, credita aluno e registra transacao
    Service->>Email: registra email para aluno
    Service->>Email: registra email para professor
    Service->>MQ: publica MOEDAS_ENVIADAS
    API-->>React: sucesso
    React-->>Professor: atualiza painel e extrato
    Email-->>Aluno: notificacao de recebimento
```

## Diagrama de sequencia - consultar extrato

```mermaid
sequenceDiagram
    actor Usuario as Aluno ou Professor
    participant React as Frontend React
    participant API as ApiController
    participant Service as AlunoService/MoedaService
    participant Repo as TransacaoRepository
    participant DB as Banco via ORM

    Usuario->>React: abre o painel
    React->>API: GET /api/aluno/dashboard ou /api/professor/dashboard
    API->>Service: busca dados do perfil
    Service->>Repo: consulta transacoes do usuario
    Repo->>DB: query ordenada por data
    DB-->>Repo: transacoes
    Repo-->>Service: extrato
    Service-->>API: dados do painel
    API-->>React: saldo, extrato e notificacoes
    React-->>Usuario: exibe historico com filtros por periodo
```

## Diagrama de sequencia - cadastrar/listar vantagem

```mermaid
sequenceDiagram
    actor Empresa
    actor Aluno
    participant React as Frontend React
    participant API as ApiController
    participant Service as VantagemService
    participant DB as Banco via ORM

    Empresa->>React: preenche titulo, descricao, foto e custo
    React->>API: POST /api/empresa/vantagens
    API->>Service: cadastrar(empresaId, dados)
    Service->>DB: persiste vantagem
    API-->>React: vantagem cadastrada

    Aluno->>React: abre catalogo
    React->>API: GET /api/aluno/dashboard
    API->>Service: catalogo()
    Service->>DB: lista vantagens ativas
    API-->>React: catalogo com status do aluno
    React-->>Aluno: mostra vantagens disponiveis, adquiridas e metas
```

## Diagrama de sequencia - trocar vantagem

```mermaid
sequenceDiagram
    actor Aluno
    participant React as Frontend React
    participant API as ApiController
    participant Service as VantagemService
    participant DB as Banco via ORM
    participant Email as EmailOutbox
    participant MQ as RabbitMQ obrigatorio
    actor Empresa

    Aluno->>React: clica em resgatar vantagem
    React->>API: POST /api/aluno/resgates
    API->>Service: resgatar(alunoId, vantagemId)
    Service->>DB: consulta aluno e vantagem ativa
    Service->>Service: valida saldo e bloqueia resgate duplicado
    Service->>Service: gera codigo unico do cupom
    Service->>DB: debita saldo e registra transacao de resgate
    Service->>Email: envia cupom com QR Code para aluno
    Service->>Email: envia cupom com QR Code para empresa
    Service->>MQ: publica CUPOM_GERADO
    API-->>React: codigo e URL do QR Code
    React-->>Aluno: mostra cupom e QR Code
    Email-->>Empresa: aviso de cupom pendente
```

## Diagrama de comunicacao - validacao de cupom

```mermaid
flowchart LR
    Aluno["Aluno apresenta cupom/QR Code"] --> Parceiro["Empresa parceira"]
    Parceiro --> ReactEmpresa["Painel da empresa"]
    ReactEmpresa --> API["ApiController"]
    API --> Service["VantagemService"]
    Service --> Repo["TransacaoRepository"]
    Repo --> DB["Banco de dados"]
    Service --> Email["EmailOutbox"]
    Service --> MQ["RabbitMQ obrigatorio"]
    Email --> AlunoEmail["Aluno recebe confirmacao"]
    MQ --> Auditoria["Consumidores futuros / auditoria"]
    DB --> ReactEmpresa
    ReactEmpresa --> Parceiro
```

## Diagrama de implantacao

```mermaid
flowchart TB
    Dev["GitHub Repository"] --> Vercel["Vercel - frontend React"]
    Dev --> Render["Render - backend Quarkus"]
    Vercel --> Browser["Navegador do usuario"]
    Browser --> Render
    Render --> H2["Banco H2 em memoria para laboratorio"]
    Render --> ViaCep["API ViaCEP"]
    Render --> Rabbit["RabbitMQ obrigatorio"]
    Render --> EmailOutbox["Tabela emails_notificacao"]

    subgraph Perfis
        Aluno["Aluno"]
        Professor["Professor"]
        Empresa["Empresa parceira"]
    end

    Aluno --> Browser
    Professor --> Browser
    Empresa --> Browser
```

## Estrategia de implantacao

- Em laboratorio, rode tudo pelo Quarkus em `http://localhost:8080`.
- No Render, use o `Dockerfile` e o `render.yaml` da pasta do projeto.
- No Vercel, use o `vercel.json` e configure `VITE_API_BASE_URL` apontando para a URL do backend no Render.
- Para cookies entre Vercel e Render, o backend usa `VALORIZA_COOKIE_SAME_SITE=None` e `VALORIZA_COOKIE_SECURE=true` no ambiente de nuvem.
- Configure um RabbitMQ acessivel no ambiente de deploy; sem ele, operacoes que publicam eventos sao bloqueadas.
- O banco H2 em memoria atende ao prototipo academico; em producao real, a troca recomendada e PostgreSQL.
