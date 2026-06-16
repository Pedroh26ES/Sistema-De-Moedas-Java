# DiagramaDeSequencia - UC-19 - Ampliar QR Code do cupom

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-02-aluno.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant QR as Gerador de QR Code
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Aluno,QR: 4. Ampliar QR Code do cupom
    Aluno->>Interface: 4.1 selecionarAmpliarQrCode(codigoCupom)
    Interface->>QR: 4.2 gerarImagemAmpliada(codigoCupom)
    activate QR
    QR-->>Interface: 4.3 qrCodeAmpliado
    deactivate QR
    opt Cupom ainda pendente
        Interface-->>Aluno: 4.4 modalComQrCodeEInstrucao
    end
```

