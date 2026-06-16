# DiagramaDeSequencia - RF-16 - Integrar WhatsApp via WAHA

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant WAHA
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Notificacao as Sistema de Notificacao

    Note over Usuario,Notificacao: 1. Atendimento guiado via WhatsApp
    Usuario->>WAHA: 1.1 enviarMensagemWhatsApp(texto)
    activate WAHA
    WAHA->>Sistema: 1.2 webhookMensagem(chatId, texto)
    activate Sistema
    Sistema->>DB: 1.3 buscarSessaoUsuarioOuTelefone(chatId)
    activate DB
    DB-->>Sistema: 1.4 contextoAtendimento
    deactivate DB
    alt Usuario nao autenticado
        Sistema-->>WAHA: 1.5 solicitarLoginOuEmail()
        WAHA-->>Usuario: 1.6 mensagemDeOrientacao
    else Usuario autenticado
        Sistema->>Sistema: 1.7 processarComandoPorPerfil(texto)
        Sistema-->>WAHA: 1.8 respostaDoComando
        WAHA-->>Usuario: 1.9 respostaWhatsApp
    end
    opt Notificacao operacional por WhatsApp
        activate Notificacao
        Notificacao->>WAHA: 1.10 enviarMensagem(chatId, conteudo)
        WAHA-->>Usuario: 1.11 avisoRecebido
        deactivate Notificacao
    end
    deactivate Sistema
    deactivate WAHA
```

