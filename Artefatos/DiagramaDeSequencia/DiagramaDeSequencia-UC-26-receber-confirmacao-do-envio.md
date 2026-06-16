# DiagramaDeSequencia - UC-26 - Receber confirmacao do envio

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-03-professor.md)

```mermaid
sequenceDiagram
    actor Professor
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Sistema,Notificacao: 3. Receber confirmacao do envio
    Sistema->>Notificacao: 3.1 enviarConfirmacaoProfessor(dadosEnvio)
    activate Notificacao
    Notificacao-->>Sistema: 3.2 emailRegistradoOuEnviado
    opt WhatsApp habilitado
        Notificacao-->>Professor: 3.3 mensagemWhatsAppEnviada
    end
    deactivate Notificacao
```

