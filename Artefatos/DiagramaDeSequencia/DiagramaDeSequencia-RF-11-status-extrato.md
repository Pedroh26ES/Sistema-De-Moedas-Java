# DiagramaDeSequencia - RF-11 - Mostrar status claro no extrato

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Usuario,DB: 1. Exibicao de status claro no extrato
    Usuario->>Interface: 1.1 abrirExtrato(perfil)
    Interface->>Sistema: 1.2 carregarExtrato(perfil, usuario)
    activate Sistema
    Sistema->>DB: 1.3 buscarCreditosResgatesECupons(usuario)
    activate DB
    DB-->>Sistema: 1.4 transacoes
    deactivate DB
    loop Para cada transacao
        Sistema->>Sistema: 1.5 classificarStatus(credito, resgate, pendente, validado)
    end
    Sistema-->>Interface: 1.6 extratoComStatus
    deactivate Sistema
    Interface-->>Usuario: 1.7 exibirStatusClaramente
```

