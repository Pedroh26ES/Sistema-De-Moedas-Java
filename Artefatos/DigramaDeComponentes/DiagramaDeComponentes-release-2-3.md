# DiagramaDeComponentes - release 2-3

Artefato das Releases 2 e 3 do Valoriza Ae.

Este diagrama apresenta os componentes internos e externos que participam dos fluxos adicionados nas Releases 2 e 3.

## Diagrama de componentes

```mermaid
flowchart TB
    subgraph Cliente["Cliente web"]
        Browser["Navegador do usuario"]
        React["Frontend React + Vite"]
        Pages["Paginas: Home, Login, Cadastro, Aluno, Professor, Empresa"]
        UI["Componentes de UI, filtros, cards, QR modal e notificacoes"]
        ApiClient["Cliente HTTP / services"]
    end

    subgraph ApiLayer["API Quarkus"]
        ApiController["ApiController /api"]
        WebControllers["HomeController, AuthController, CadastroController, Web"]
        Dtos["DTOs de request e response"]
        ExceptionMapper["RegraNegocioExceptionMapper"]
    end

    subgraph Security["Seguranca e sessoes"]
        SessaoService["SessaoService"]
        SenhaService["SenhaService"]
        RecuperacaoSenha["RecuperacaoSenhaService"]
    end

    subgraph Business["Services de negocio"]
        CadastroService["CadastroService"]
        AlunoService["AlunoService"]
        MoedaService["MoedaService"]
        VantagemService["VantagemService"]
        EmailTemplate["EmailTemplateService"]
        EmailGateway["EmailOutboxGateway"]
        RabbitService["RabbitMqFilaService"]
        ViaCepService["ViaCepService"]
        QrCodeService["QrCodeService"]
        SemestreUtil["SemestreUtil"]
    end

    subgraph Persistence["Persistencia JPA / Panache"]
        Repositories["Repositories Panache"]
        Entities["Entidades JPA"]
        H2[(Banco H2 em memoria)]
        Outbox[(emails_notificacao)]
        LocalQueue[(eventos_fila_local)]
        ResetTokens[(redefinicoes_senha)]
    end

    subgraph External["Integracoes externas"]
        EmailJS["EmailJS"]
        RabbitMQ["RabbitMQ"]
        ViaCEP["API ViaCEP"]
        ZXing["ZXing"]
    end

    subgraph DevOps["Execucao e deploy"]
        Maven["Maven / Quarkus dev"]
        Npm["npm build:frontend"]
        Docker["Docker Compose"]
        Render["Render backend"]
        Vercel["Vercel frontend"]
    end

    Browser --> React
    React --> Pages
    Pages --> UI
    UI --> ApiClient
    ApiClient --> ApiController
    Browser --> WebControllers

    ApiController --> Dtos
    ApiController --> SessaoService
    ApiController --> CadastroService
    ApiController --> AlunoService
    ApiController --> MoedaService
    ApiController --> VantagemService
    ApiController --> RecuperacaoSenha
    ApiController --> ViaCepService
    ApiController --> QrCodeService
    ApiController --> ExceptionMapper

    SessaoService --> SenhaService
    RecuperacaoSenha --> SenhaService
    RecuperacaoSenha --> EmailGateway
    CadastroService --> SenhaService
    CadastroService --> Repositories
    AlunoService --> Repositories
    MoedaService --> Repositories
    VantagemService --> Repositories
    MoedaService --> EmailTemplate
    VantagemService --> EmailTemplate
    EmailTemplate --> EmailGateway
    MoedaService --> EmailGateway
    VantagemService --> EmailGateway
    MoedaService --> RabbitService
    VantagemService --> RabbitService
    VantagemService --> QrCodeService
    MoedaService --> SemestreUtil

    Repositories --> Entities
    Entities --> H2
    EmailGateway --> Outbox
    EmailGateway --> EmailJS
    RabbitService --> RabbitMQ
    RabbitService -. fallback local .-> LocalQueue
    RecuperacaoSenha --> ResetTokens
    ViaCepService --> ViaCEP
    QrCodeService --> ZXing

    Npm --> React
    Maven --> ApiLayer
    Docker --> RabbitMQ
    Docker --> H2
    Vercel --> React
    Render --> ApiLayer
```

## Componentes cobertos

- Frontend React: navegacao por perfil, paineis, formularios, filtros, catalogo, cupom e QR Code.
- Backend Quarkus: controllers, services, seguranca, DTOs, exception mapper e regras de negocio.
- Persistencia: entidades JPA, repositories Panache, outbox de email, tokens de senha e fallback de fila.
- Integracoes: EmailJS, RabbitMQ, ViaCEP e ZXing.
