# DiagramaDeSequencia - UC-40 - Persistir evento local em desenvolvimento

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-05-integracoes-rastreabilidade.md)

```mermaid
sequenceDiagram
    participant Servico as Servico de Negocio
    participant Notificacao as Sistema de Notificacao
    participant DB as Banco de Dados
    participant EmailJS
    participant QR as Gerador de QR Code
    participant Fila as RabbitMQ

    Note over Fila,DB: 4. Persistir evento local em desenvolvimento
    Fila->>DB: 4.1 salvarEventoLocal(tipo, dados)
    activate DB
    DB-->>Fila: 4.2 fallbackRegistrado
    deactivate DB
```

