# DiagramaDeSequencia - UC-14 - Consultar catalogo de vantagens

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-02-aluno.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Aluno,DB: 3. Consultar catalogo de vantagens
    Aluno->>Interface: 3.1 abrirCatalogo()
    Interface->>Sistema: 3.2 listarVantagensAtivas()
    activate Sistema
    Sistema->>DB: 3.3 consultarVantagensAtivas()
    activate DB
    DB-->>Sistema: 3.4 catalogo
    deactivate DB
    Sistema-->>Interface: 3.5 vantagensDisponiveis
    deactivate Sistema
    Interface-->>Aluno: 3.6 exibirCatalogo
```

