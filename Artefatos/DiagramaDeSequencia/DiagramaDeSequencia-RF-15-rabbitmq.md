# DiagramaDeSequencia - RF-15 - Publicar eventos em RabbitMQ

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    participant Servico as Servico de Negocio
    participant Fila as RabbitMQ
    participant DB as Banco de Dados

    Note over Servico,DB: 1. Publicacao de evento operacional
    Servico->>Fila: 1.1 publicarEvento(tipo, dados)
    activate Fila
    alt RabbitMQ disponivel
        Fila-->>Servico: 1.2 eventoPublicado
    else RabbitMQ indisponivel em desenvolvimento
        Fila-->>Servico: 1.3 falhaPublicacao
        Servico->>DB: 1.4 persistirEventoLocal(tipo, dados)
        activate DB
        DB-->>Servico: 1.5 eventoLocalRegistrado
        deactivate DB
    end
    deactivate Fila
```

