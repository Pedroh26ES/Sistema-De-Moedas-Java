# DiagramaDeSequencia - RF-10 - Impedir compra duplicada do mesmo beneficio

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Aluno,DB: 1. Bloqueio de resgate duplicado
    Aluno->>Interface: 1.1 tentarResgatarMesmoBeneficio(vantagem)
    activate Interface
    Interface->>Sistema: 1.2 verificarDuplicidade(aluno, vantagem)
    activate Sistema
    Sistema->>DB: 1.3 buscarCupomPendenteOuAtivo(aluno, vantagem)
    activate DB
    DB-->>Sistema: 1.4 cupomExistenteOuNao
    alt Cupom pendente ou ativo existente
        Sistema-->>Interface: 1.5 duplicidadeBloqueada
        Interface-->>Aluno: 1.6 informarCupomJaExistente
    else Nenhum cupom ativo
        Sistema-->>Interface: 1.7 resgatePermitido
        Interface-->>Aluno: 1.8 permitirContinuacao
    end
    deactivate DB
    deactivate Sistema
    deactivate Interface
```

