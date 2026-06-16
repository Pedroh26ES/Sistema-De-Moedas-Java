# DiagramaDeSequencia - RF-12 - Filtrar extratos e notificacoes por periodo

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Usuario,DB: 1. Filtro por periodo em extratos e notificacoes
    Usuario->>Interface: 1.1 selecionarFiltro(dia, semana, mes, ano, todos)
    activate Interface
    Interface->>Sistema: 1.2 aplicarFiltroPeriodo(usuario, filtro)
    activate Sistema
    Sistema->>DB: 1.3 consultarRegistrosPorPeriodo(usuario, filtro)
    activate DB
    DB-->>Sistema: 1.4 registrosFiltrados
    deactivate DB
    alt Existem registros no periodo
        Sistema-->>Interface: 1.5 listaFiltrada
        Interface-->>Usuario: 1.6 exibirRegistros
    else Nenhum registro no periodo
        Sistema-->>Interface: 1.7 listaVazia
        Interface-->>Usuario: 1.8 exibirMensagemSemRegistros
    end
    deactivate Sistema
    deactivate Interface
```

