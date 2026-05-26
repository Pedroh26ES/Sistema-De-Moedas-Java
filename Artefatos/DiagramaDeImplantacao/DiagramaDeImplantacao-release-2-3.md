# DiagramaDeImplantacao - release 2-3

Artefato das Releases 2 e 3 do Valoriza Ae.

Este diagrama mostra como o sistema roda localmente, em Docker e em um cenario de deploy separado entre frontend e backend.

## Diagrama de implantacao

```mermaid
flowchart TB
    Dev["Desenvolvedor / GitHub"]

    subgraph LocalTerminal["Ambiente local pelo terminal"]
        PowerShell["PowerShell"]
        NpmLocal["npm run build:frontend"]
        QuarkusDev["mvn quarkus:dev"]
        AppLocal["Aplicacao em localhost:8080"]
        H2Local[(H2 em memoria)]
        FallbackLocal[(eventos_fila_local)]
    end

    subgraph DockerLocal["Ambiente local com Docker"]
        Compose["docker compose / start.ps1"]
        AppContainer["Container Quarkus"]
        RabbitContainer["Container RabbitMQ"]
        RabbitPanel["Management localhost:15672"]
    end

    subgraph Nuvem["Cenario de deploy"]
        Vercel["Vercel - frontend React"]
        Render["Render - backend Quarkus"]
        BancoProd[(Banco relacional recomendado)]
        RabbitProd["RabbitMQ gerenciado"]
    end

    subgraph Externos["Servicos externos"]
        EmailJS["EmailJS"]
        ViaCEP["ViaCEP"]
    end

    Usuario["Usuario no navegador"]

    Dev --> PowerShell
    PowerShell --> NpmLocal
    NpmLocal --> QuarkusDev
    QuarkusDev --> AppLocal
    AppLocal --> H2Local
    AppLocal -. fallback se RabbitMQ indisponivel .-> FallbackLocal
    AppLocal --> EmailJS
    AppLocal --> ViaCEP
    Usuario --> AppLocal

    Dev --> Compose
    Compose --> AppContainer
    Compose --> RabbitContainer
    RabbitContainer --> RabbitPanel
    AppContainer --> RabbitContainer
    AppContainer --> EmailJS
    AppContainer --> ViaCEP

    Dev --> Vercel
    Dev --> Render
    Usuario --> Vercel
    Vercel --> Render
    Render --> BancoProd
    Render --> RabbitProd
    Render --> EmailJS
    Render --> ViaCEP
```

## Nos e responsabilidades

- localhost:8080: entrega frontend compilado e API Quarkus no laboratorio.
- H2: banco em memoria usado para execucao local e testes academicos.
- RabbitMQ: fila real dos eventos de negocio quando executado com Docker ou deploy.
- eventos_fila_local: fallback de rastreabilidade usado em desenvolvimento quando habilitado.
- EmailJS: envio real de emails de moedas, cupons, validacoes e recuperacao de senha.
- ViaCEP: consulta de endereco por CEP nos cadastros.
- Vercel e Render: separacao recomendada entre frontend e backend em deploy.
