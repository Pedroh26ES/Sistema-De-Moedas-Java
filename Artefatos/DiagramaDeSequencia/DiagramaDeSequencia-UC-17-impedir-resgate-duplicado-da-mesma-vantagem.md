# DiagramaDeSequencia - UC-17 - Impedir resgate duplicado da mesma vantagem

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-02-aluno.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant QR as Gerador de QR Code
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Aluno,DB: 2. Impedir resgate duplicado da mesma vantagem
    Aluno->>Interface: 2.1 tentarResgatarVantagemJaComprada(vantagem)
    Interface->>Sistema: 2.2 validarDuplicidade(aluno, vantagem)
    activate Sistema
    Sistema->>DB: 2.3 buscarCupomAtivoDaVantagem(aluno, vantagem)
    activate DB
    DB-->>Sistema: 2.4 cupomExistente
    deactivate DB
    alt Cupom pendente ou ativo encontrado
        Sistema-->>Interface: 2.5 duplicidadeBloqueada
        Interface-->>Aluno: 2.6 "Voce ja possui cupom para esta vantagem."
    else Nenhum cupom ativo
        Sistema-->>Interface: 2.7 resgatePodeContinuar
    end
    deactivate Sistema
```

