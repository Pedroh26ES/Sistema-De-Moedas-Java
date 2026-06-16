# DiagramaDeSequencia - UC-32 - Excluir vantagem sem historico de cupom

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

    Note over Empresa,DB: 2. Excluir vantagem sem historico de cupom
    Empresa->>Interface: 2.1 solicitarExclusaoVantagem(id)
    Interface->>Sistema: 2.2 excluirVantagem(empresa, id)
    activate Sistema
    Sistema->>DB: 2.3 verificarHistoricoDeResgates(id)
    activate DB
    DB-->>Sistema: 2.4 historicoDaVantagem
    alt Sem historico de cupom
        Sistema->>DB: 2.5 removerVantagem(id)
        DB-->>Sistema: 2.6 vantagemRemovida
        Sistema-->>Interface: 2.7 exclusaoConcluida
        Interface-->>Empresa: 2.8 vantagemRemovidaDaLista
    else Possui cupom ou resgate
        Sistema-->>Interface: 2.9 exclusaoBloqueada
        Interface-->>Empresa: 2.10 "Vantagem possui historico. Use pausar."
    end
    deactivate DB
    deactivate Sistema
```

