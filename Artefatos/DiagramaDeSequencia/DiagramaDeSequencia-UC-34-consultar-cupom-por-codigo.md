# DiagramaDeSequencia - UC-34 - Consultar cupom por codigo

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

    Note over Empresa,DB: 4. Consultar cupom por codigo
    Empresa->>Interface: 4.1 informarCodigoCupom(codigo)
    Interface->>Sistema: 4.2 consultarCupom(empresa, codigo)
    activate Sistema
    Sistema->>DB: 4.3 buscarCupomPorCodigo(codigo)
    activate DB
    DB-->>Sistema: 4.4 dadosCupom
    alt Cupom encontrado e pertence a empresa
        Sistema-->>Interface: 4.5 detalhesDoCupom
        Interface-->>Empresa: 4.6 exibirDadosParaConferencia
    else Cupom inexistente ou de outra empresa
        Sistema-->>Interface: 4.7 consultaBloqueada
        Interface-->>Empresa: 4.8 "Cupom nao encontrado para esta empresa."
    end
    deactivate DB
    deactivate Sistema
```

