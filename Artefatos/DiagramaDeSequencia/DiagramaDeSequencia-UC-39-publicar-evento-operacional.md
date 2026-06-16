# DiagramaDeSequencia - UC-39 - Publicar evento operacional

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

    Note over Servico,Fila: 3. Publicar evento operacional
    Servico->>Fila: 3.1 publicarEvento(tipo, dados)
    activate Fila
    alt RabbitMQ disponivel
        Fila-->>Servico: 3.2 eventoPublicado
    else Falha de conexao com RabbitMQ
        Fila-->>Servico: 3.3 acionarFallbackLocal
    end
    deactivate Fila
```

