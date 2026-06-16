# DiagramaDeSequencia - RF-14 - Enviar emails reais pelo EmailJS

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    participant Servico as Servico de Negocio
    participant Notificacao as Sistema de Notificacao
    participant DB as Banco de Dados
    participant EmailJS
    actor Usuario

    Note over Servico,EmailJS: 1. Envio de email real e registro de notificacao
    Servico->>Notificacao: 1.1 enviarEmail(destinatario, assunto, conteudo)
    activate Notificacao
    Notificacao->>DB: 1.2 registrarNotificacao(destinatario, assunto)
    activate DB
    DB-->>Notificacao: 1.3 notificacaoRegistrada
    deactivate DB
    alt EmailJS habilitado e dominio permitido
        Notificacao->>EmailJS: 1.4 enviarTemplate(payload)
        activate EmailJS
        EmailJS-->>Notificacao: 1.5 envioAceito
        deactivate EmailJS
        Notificacao-->>Usuario: 1.6 emailRecebido
    else EmailJS desabilitado ou dominio ignorado
        Notificacao-->>Servico: 1.7 apenasRegistroNoPainel
    end
    deactivate Notificacao
```

