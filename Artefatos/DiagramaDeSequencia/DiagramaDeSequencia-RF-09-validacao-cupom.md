# DiagramaDeSequencia - RF-09 - Empresa valida cupom antes do beneficio

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Empresa
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Empresa,Fila: 1. Validacao de cupom pela empresa
    Empresa->>Interface: 1.1 informarCodigoCupom(codigo)
    Interface->>Sistema: 1.2 consultarCupom(empresa, codigo)
    activate Sistema
    Sistema->>DB: 1.3 buscarCupomDaEmpresa(codigo, empresa)
    activate DB
    DB-->>Sistema: 1.4 dadosCupom
    alt Cupom valido e pendente
        Sistema-->>Interface: 1.5 cupomDisponivelParaValidacao
        Empresa->>Interface: 1.6 confirmarEntregaBeneficio()
        Interface->>Sistema: 1.7 validarCupom(empresa, codigo)
        Sistema->>DB: 1.8 marcarCupomValidado(codigo)
        DB-->>Sistema: 1.9 validacaoSalva
        Sistema->>Notificacao: 1.10 notificarAlunoCupomValidado()
        Sistema->>Fila: 1.11 publicarCUPOM_VALIDADO()
        Sistema-->>Interface: 1.12 validacaoConcluida
        Interface-->>Empresa: 1.13 atendimentoConfirmado
    else Cupom invalido
        Sistema-->>Interface: 1.14 validacaoBloqueada(motivo)
        Interface-->>Empresa: 1.15 exibirMotivoDoBloqueio
    end
    deactivate DB
    deactivate Sistema
```

