# DiagramaDeSequencia - UC-18 - Ver cupom, status e QR Code

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

    Note over Aluno,QR: 3. Ver cupom, status e QR Code
    Aluno->>Interface: 3.1 abrirMeusCupons()
    Interface->>Sistema: 3.2 listarCupons(aluno)
    activate Sistema
    Sistema->>DB: 3.3 consultarCuponsDoAluno(aluno)
    activate DB
    DB-->>Sistema: 3.4 cuponsEStatus
    deactivate DB
    Sistema-->>Interface: 3.5 listaDeCupons
    deactivate Sistema
    Interface->>QR: 3.6 carregarQrCode(codigoCupom)
    activate QR
    QR-->>Interface: 3.7 imagemQrCode
    deactivate QR
    Interface-->>Aluno: 3.8 cupomStatusQrCodeExibidos
```

