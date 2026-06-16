# DiagramaDeSequencia - RF-07 - Aluno resgata vantagens disponiveis

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Fila as RabbitMQ

    Note over Aluno,Fila: 1. Resgate de vantagem disponivel
    Aluno->>Interface: 1.1 selecionarVantagem(vantagem)
    activate Interface
    Interface->>Sistema: 1.2 resgatarVantagem(aluno, vantagem)
    activate Sistema
    Sistema->>DB: 1.3 validarSaldoStatusEDuplicidade(aluno, vantagem)
    activate DB
    DB-->>Sistema: 1.4 resultadoValidacao
    alt Resgate nao permitido
        Sistema-->>Interface: 1.5 resgateNegado(motivo)
        Interface-->>Aluno: 1.6 informarMotivo
    else Resgate permitido
        Sistema->>DB: 1.7 descontarMoedasECriarResgate()
        DB-->>Sistema: 1.8 resgateRegistrado
        Sistema->>Fila: 1.9 publicarCUPOM_GERADO()
        activate Fila
        Fila-->>Sistema: 1.9.1 eventoPublicado
        deactivate Fila
        Sistema-->>Interface: 1.10 resgateConfirmado
        Interface-->>Aluno: 1.11 exibirCupomGerado
    end
    deactivate DB
    deactivate Sistema
    deactivate Interface
```

