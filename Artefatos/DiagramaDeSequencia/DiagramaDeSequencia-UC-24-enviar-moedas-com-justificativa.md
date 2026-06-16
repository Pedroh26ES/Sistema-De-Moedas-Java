# DiagramaDeSequencia - UC-24 - Enviar moedas com justificativa

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

    Note over Professor,Fila: 1. Enviar moedas com justificativa
    Professor->>Interface: 1.1 enviarMoedas(aluno, valor, motivo)
    Interface->>Sistema: 1.2 registrarEnvioMoedas(professor, aluno, valor, motivo)
    activate Sistema
    Sistema->>DB: 1.3 consultarProfessorAlunoESaldo(professor, aluno)
    activate DB
    DB-->>Sistema: 1.4 dadosParaEnvio
    alt Saldo insuficiente ou motivo ausente
        Sistema-->>Interface: 1.5 envioNegado(motivo)
        Interface-->>Professor: 1.6 "Revise saldo, valor ou justificativa."
    else Envio permitido
        Sistema->>DB: 1.7 debitarProfessorCreditarAluno(professor, aluno, valor)
        Sistema->>DB: 1.8 registrarTransacaoEnvio(motivo)
        DB-->>Sistema: 1.9 transacaoRegistrada
        Sistema->>Notificacao: 1.10 notificarAlunoEProfessor()
        Sistema->>Fila: 1.11 publicarMOEDAS_ENVIADAS()
        Sistema-->>Interface: 1.12 envioConfirmado
        Interface-->>Professor: 1.13 cotaExtratoAtualizados
    end
    deactivate DB
    deactivate Sistema
```

