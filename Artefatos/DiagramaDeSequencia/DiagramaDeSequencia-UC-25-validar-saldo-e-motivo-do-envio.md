# DiagramaDeSequencia - UC-25 - Validar saldo e motivo do envio

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

    Note over Professor,DB: 2. Validar saldo e motivo do envio
    Professor->>Interface: 2.1 confirmarDadosDoEnvio()
    Interface->>Sistema: 2.2 validarSaldoEMotivo(professor, valor, motivo)
    activate Sistema
    Note right of Sistema: Motivo deve existir e professor precisa ter cota suficiente
    Sistema->>DB: 2.3 buscarSaldoDoProfessor(professor)
    activate DB
    DB-->>Sistema: 2.4 saldoAtual
    deactivate DB
    alt Valor positivo, saldo suficiente e motivo informado
        Sistema-->>Interface: 2.5 validacaoAprovada
        Interface-->>Professor: 2.6 envioPodeSerConfirmado
    else Regra invalida
        Sistema-->>Interface: 2.7 validacaoReprovada
        Interface-->>Professor: 2.8 mensagemDaRegraViolada
    end
    deactivate Sistema
```

