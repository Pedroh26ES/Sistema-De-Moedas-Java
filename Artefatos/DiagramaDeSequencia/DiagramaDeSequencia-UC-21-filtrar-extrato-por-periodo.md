# DiagramaDeSequencia - UC-21 - Filtrar extrato por periodo

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-03-professor.md)

```mermaid
sequenceDiagram
    actor Professor
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Professor,DB: 2. Filtrar extrato por periodo
    Professor->>Interface: 2.1 selecionarPeriodo(periodo)
    Interface->>Sistema: 2.2 filtrarExtratoProfessor(periodo)
    activate Sistema
    Sistema->>DB: 2.3 consultarEnviosFiltrados(professor, periodo)
    activate DB
    DB-->>Sistema: 2.4 enviosDoPeriodo
    deactivate DB
    Sistema-->>Interface: 2.5 extratoFiltrado
    deactivate Sistema
    Interface-->>Professor: 2.6 extratoAtualizado
```

