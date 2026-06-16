# DiagramaDeSequencia - UC-20 - Ver cota, alunos, extrato e notificacoes

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-03-professor.md)

```mermaid
sequenceDiagram
    actor Professor
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Professor,DB: 1. Ver cota, alunos, extrato e notificacoes
    Professor->>Interface: 1.1 abrirPainelProfessor()
    Interface->>Sistema: 1.2 carregarDashboardProfessor(professor)
    activate Sistema
    Sistema->>DB: 1.3 buscarCotaAlunosExtratoNotificacoes(professor)
    activate DB
    DB-->>Sistema: 1.4 dadosProfessor
    deactivate DB
    Sistema-->>Interface: 1.5 dashboardProfessor
    deactivate Sistema
    Interface-->>Professor: 1.6 exibirCotaAlunosExtratoNotificacoes
```

