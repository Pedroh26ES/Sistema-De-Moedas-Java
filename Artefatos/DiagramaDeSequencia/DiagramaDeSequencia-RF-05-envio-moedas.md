# DiagramaDeSequencia - RF-05 - Enviar moedas com justificativa

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Professor
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Professor,Fila: 1. Envio de moedas ao aluno
    Professor->>Interface: 1.1 informarAlunoValorMotivo(aluno, valor, motivo)
    Interface->>Sistema: 1.2 enviarMoedas(professor, aluno, valor, motivo)
    activate Sistema
    Note right of Sistema: Justificativa obrigatoria e desconto da cota semestral
    Sistema->>DB: 1.3 validarProfessorAlunoSaldoMotivo()
    activate DB
    DB-->>Sistema: 1.4 validacaoEnvio
    alt Envio invalido
        Sistema-->>Interface: 1.5 envioNegado(motivo)
        Interface-->>Professor: 1.6 exibirRegraViolada
    else Envio valido
        Sistema->>DB: 1.7 debitarProfessorCreditarAluno()
        Sistema->>DB: 1.8 registrarTransacaoEnvio()
        DB-->>Sistema: 1.9 envioRegistrado
        Sistema->>Notificacao: 1.10 notificarAlunoEProfessor()
        Sistema->>Fila: 1.11 publicarMOEDAS_ENVIADAS()
        Sistema-->>Interface: 1.12 envioConfirmado
        Interface-->>Professor: 1.13 cotaExtratoAtualizados
    end
    deactivate DB
    deactivate Sistema
```

