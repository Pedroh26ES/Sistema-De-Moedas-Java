# DiagramaDeSequencia - UC-38 - Gerar QR Code do cupom

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

    Note over Servico,QR: 2. Gerar QR Code do cupom
    Servico->>QR: 2.1 gerarQrCode(urlDoCupom)
    activate QR
    QR->>QR: 2.2 codificarUrlEmImagemPng()
    QR-->>Servico: 2.3 qrCodePng
    deactivate QR
```

