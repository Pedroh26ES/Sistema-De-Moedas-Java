# DiagramaDeSequencia - UC-13 - Filtrar extrato por periodo

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-02-aluno.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Aluno,DB: 2. Filtrar extrato por periodo
    Aluno->>Interface: 2.1 selecionarPeriodo(periodo)
    Interface->>Sistema: 2.2 filtrarExtratoAluno(periodo)
    activate Sistema
    Sistema->>DB: 2.3 consultarTransacoesDoPeriodo(aluno, periodo)
    activate DB
    DB-->>Sistema: 2.4 transacoesFiltradas
    deactivate DB
    alt Existem movimentacoes
        Sistema-->>Interface: 2.5 extratoFiltrado
        Interface-->>Aluno: 2.6 listaDeMovimentacoes
    else Sem movimentacoes
        Sistema-->>Interface: 2.7 extratoVazio
        Interface-->>Aluno: 2.8 "Nenhuma movimentacao no periodo."
    end
    deactivate Sistema
```

