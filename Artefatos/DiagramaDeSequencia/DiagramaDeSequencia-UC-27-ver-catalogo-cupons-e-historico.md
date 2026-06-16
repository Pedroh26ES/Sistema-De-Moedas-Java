# DiagramaDeSequencia - UC-27 - Ver catalogo, cupons e historico

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-04-empresa-parceira.md)

```mermaid
sequenceDiagram
    actor Empresa
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Empresa,DB: 1. Ver catalogo, cupons e historico
    Empresa->>Interface: 1.1 abrirPainelEmpresa()
    Interface->>Sistema: 1.2 carregarDashboardEmpresa(empresa)
    activate Sistema
    Sistema->>DB: 1.3 buscarCatalogoCuponsHistorico(empresa)
    activate DB
    DB-->>Sistema: 1.4 dadosDaEmpresa
    deactivate DB
    Sistema-->>Interface: 1.5 dashboardEmpresa
    deactivate Sistema
    Interface-->>Empresa: 1.6 exibirCatalogoCuponsHistorico
```

