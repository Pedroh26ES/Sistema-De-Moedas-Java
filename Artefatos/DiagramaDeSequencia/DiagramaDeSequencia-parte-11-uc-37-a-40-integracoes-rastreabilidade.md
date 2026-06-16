# DiagramaDeSequencia - Integracoes e rastreabilidade - UC-37 a UC-40

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados e fragmentos `alt`.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-05-integracoes-rastreabilidade.md)

```mermaid
sequenceDiagram
    participant Servico as Servico de Negocio
    participant Notificacao as Sistema de Notificacao
    participant DB as Banco de Dados
    participant EmailJS
    participant QR as Gerador de QR Code
    participant Fila as RabbitMQ

    Note over Servico,EmailJS: 1. Enviar email real e registrar notificacao
    Servico->>Notificacao: 1.1 enviar(destinatario, assunto, conteudo)
    activate Notificacao
    Notificacao->>DB: 1.2 persistirNotificacao(destinatario, assunto)
    activate DB
    DB-->>Notificacao: 1.3 notificacaoRegistrada
    deactivate DB
    alt EmailJS habilitado e dominio permitido
        Notificacao->>EmailJS: 1.4 enviarTemplateEmailJS(payload)
        activate EmailJS
        EmailJS-->>Notificacao: 1.5 envioAceito
        deactivate EmailJS
        Notificacao-->>Servico: 1.6 emailRealEnviado
    else Email real desabilitado ou dominio ignorado
        Notificacao-->>Servico: 1.7 apenasRegistroNoPainel
    end
    deactivate Notificacao

    Note over Servico,QR: 2. Gerar QR Code do cupom
    Servico->>QR: 2.1 gerarQrCode(urlDoCupom)
    activate QR
    QR->>QR: 2.2 codificarUrlEmImagemPng()
    QR-->>Servico: 2.3 qrCodePng
    deactivate QR

    Note over Servico,Fila: 3. Publicar evento operacional
    Servico->>Fila: 3.1 publicarEvento(tipo, dados)
    activate Fila
    alt RabbitMQ disponivel
        Fila-->>Servico: 3.2 eventoPublicado
    else Falha de conexao com RabbitMQ
        Fila-->>Servico: 3.3 acionarFallbackLocal
    end
    deactivate Fila

    Note over Fila,DB: 4. Persistir evento local em desenvolvimento
    Fila->>DB: 4.1 salvarEventoLocal(tipo, dados)
    activate DB
    DB-->>Fila: 4.2 fallbackRegistrado
    deactivate DB
```
