# DiagramaDeSequencia - Professor - UC-24 a UC-26

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`/`opt`.

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

    Note over Sistema,Notificacao: 3. Receber confirmacao do envio
    Sistema->>Notificacao: 3.1 enviarConfirmacaoProfessor(dadosEnvio)
    activate Notificacao
    Notificacao-->>Sistema: 3.2 emailRegistradoOuEnviado
    opt WhatsApp habilitado
        Notificacao-->>Professor: 3.3 mensagemWhatsAppEnviada
    end
    deactivate Notificacao
```
