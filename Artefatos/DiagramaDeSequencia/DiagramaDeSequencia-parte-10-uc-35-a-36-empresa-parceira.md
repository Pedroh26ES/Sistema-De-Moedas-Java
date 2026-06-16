# DiagramaDeSequencia - Empresa parceira - UC-35 a UC-36

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`.

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

    Note over Empresa,DB: 2. Bloquear cupom usado, pausado ou de outra empresa
    Empresa->>Interface: 2.1 tentarValidarCupom(codigo)
    Interface->>Sistema: 2.2 verificarRegrasDoCupom(empresa, codigo)
    activate Sistema
    Sistema->>DB: 2.3 buscarTransacaoPorCodigo(codigo)
    activate DB
    DB-->>Sistema: 2.4 cupomEncontrado
    alt Cupom ja usado
        Sistema-->>Interface: 2.5 bloqueioCupomUsado
        Interface-->>Empresa: 2.6 "Cupom ja validado."
    else Vantagem pausada
        Sistema-->>Interface: 2.7 bloqueioVantagemPausada
        Interface-->>Empresa: 2.8 "Vantagem pausada."
    else Cupom pertence a outra empresa
        Sistema-->>Interface: 2.9 bloqueioOutraEmpresa
        Interface-->>Empresa: 2.10 "Cupom nao pertence a esta empresa."
    else Cupom pendente e valido
        Sistema-->>Interface: 2.11 validacaoPermitida
        Interface-->>Empresa: 2.12 permitirConfirmacao
    end
    deactivate DB
    deactivate Sistema
```
