# DiagramaDeSequencia - UC-36 - Bloquear cupom usado, pausado ou de outra empresa

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

