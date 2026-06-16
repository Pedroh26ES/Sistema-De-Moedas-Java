# DiagramaDeSequencia - UC-33 - Notificar aluno com cupom pendente

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

    Note over Sistema,Notificacao: 3. Notificar aluno com cupom pendente
    Sistema->>DB: 3.1 consultarCuponsPendentesDaVantagem(id)
    activate DB
    DB-->>Sistema: 3.2 alunosAfetados
    deactivate DB
    loop Para cada cupom pendente
        Sistema->>Notificacao: 3.3 registrarEEnviarAviso(aluno, cupom, status)
        Notificacao-->>Sistema: 3.4 avisoRegistrado
    end
```

