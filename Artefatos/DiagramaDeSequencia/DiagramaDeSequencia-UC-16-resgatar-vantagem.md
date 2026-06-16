# DiagramaDeSequencia - UC-16 - Resgatar vantagem

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

    Note over Aluno,Fila: 1. Resgatar vantagem
    Aluno->>Interface: 1.1 solicitarResgate(vantagem)
    Interface->>Sistema: 1.2 resgatarVantagem(aluno, vantagem)
    activate Sistema
    Note right of Sistema: Valida saldo, status da vantagem e duplicidade
    Sistema->>DB: 1.3 consultarAlunoVantagemECupons(aluno, vantagem)
    activate DB
    DB-->>Sistema: 1.4 dadosDoResgate
    alt Resgate nao permitido
        Sistema-->>Interface: 1.5 resgateNegado(motivo)
        Interface-->>Aluno: 1.6 "Nao foi possivel resgatar a vantagem."
    else Resgate permitido
        Sistema->>DB: 1.7 debitarSaldoECriarCupom(aluno, vantagem)
        DB-->>Sistema: 1.8 cupomPendenteSalvo
        Sistema->>QR: 1.9 gerarQrCode(cupom)
        activate QR
        QR-->>Sistema: 1.10 qrCodeGerado
        deactivate QR
        Sistema->>Notificacao: 1.11 notificarAlunoEEmpresa(cupom)
        Sistema->>Fila: 1.12 publicarCUPOM_GERADO(cupom)
        Sistema-->>Interface: 1.13 codigoStatusQrCode
        Interface-->>Aluno: 1.14 cupomPendenteExibido
    end
    deactivate DB
    deactivate Sistema
```

