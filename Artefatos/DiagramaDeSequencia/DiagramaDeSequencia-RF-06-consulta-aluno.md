# DiagramaDeSequencia - RF-06 - Aluno consulta saldo, extrato, notificacoes, catalogo e cupons

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Aluno,DB: 1. Consulta do painel do aluno
    Aluno->>Interface: 1.1 abrirPainelAluno()
    activate Interface
    Interface->>Sistema: 1.2 carregarDadosAluno(aluno)
    activate Sistema
    Sistema->>DB: 1.3 buscarSaldoExtratoNotificacoesCatalogoCupons(aluno)
    activate DB
    DB-->>Sistema: 1.4 dadosConsolidados
    deactivate DB
    alt Dados encontrados
        Sistema-->>Interface: 1.5 painelCompleto
        Interface-->>Aluno: 1.6 exibirSaldoExtratoCatalogoCupons
    else Sem movimentacoes ou cupons
        Sistema-->>Interface: 1.7 painelComListasVazias
        Interface-->>Aluno: 1.8 exibirEstadoInicial
    end
    deactivate Sistema
    deactivate Interface
```

