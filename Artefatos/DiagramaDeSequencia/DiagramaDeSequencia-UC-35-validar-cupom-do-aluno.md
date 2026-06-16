# DiagramaDeSequencia - UC-35 - Validar cupom do aluno

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-04-empresa-parceira.md)

```mermaid
sequenceDiagram
    actor Empresa
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Empresa,Fila: 1. Validar cupom do aluno
    Empresa->>Interface: 1.1 confirmarValidacaoCupom(codigo)
    Interface->>Sistema: 1.2 validarCupom(empresa, codigo)
    activate Sistema
    Note right of Sistema: Cupom precisa ser da empresa, estar pendente e vantagem ativa
    Sistema->>DB: 1.3 consultarCupomEVantagem(codigo)
    activate DB
    DB-->>Sistema: 1.4 dadosCupom
    alt Cupom valido para validacao
        Sistema->>DB: 1.5 marcarCupomValidado(codigo)
        DB-->>Sistema: 1.6 validacaoSalva
        Sistema->>Notificacao: 1.7 notificarAlunoCupomValidado(cupom)
        Sistema->>Fila: 1.8 publicarCUPOM_VALIDADO(cupom)
        Sistema-->>Interface: 1.9 validacaoConcluida
        Interface-->>Empresa: 1.10 atendimentoConfirmado
    else Cupom invalido
        Sistema-->>Interface: 1.11 validacaoRecusada(motivo)
        Interface-->>Empresa: 1.12 motivoDoBloqueio
    end
    deactivate DB
    deactivate Sistema
```

