# DiagramaDeSequencia - UC-12 - Ver saldo, extrato, notificacoes e cupons

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-02-aluno.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Aluno,DB: 1. Ver saldo, extrato, notificacoes e cupons
    Aluno->>Interface: 1.1 abrirPainelAluno()
    Interface->>Sistema: 1.2 carregarDashboardAluno(aluno)
    activate Sistema
    Sistema->>DB: 1.3 buscarSaldoExtratoNotificacoesCupons(aluno)
    activate DB
    DB-->>Sistema: 1.4 dadosConsolidados
    deactivate DB
    Sistema-->>Interface: 1.5 dashboardAluno
    deactivate Sistema
    Interface-->>Aluno: 1.6 exibirSaldoExtratoNotificacoesCupons
```

