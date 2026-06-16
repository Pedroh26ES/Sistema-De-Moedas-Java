# DiagramaDeSequencia - UC-06 - Selecionar instituicao pre-cadastrada

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-01-conta-cadastro-seguranca.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant ViaCEP

    Note over Usuario,DB: 2. Selecionar instituicao pre-cadastrada
    Usuario->>Interface: 2.1 abrirCadastroAluno()
    Interface->>Sistema: 2.2 listarInstituicoes()
    activate Sistema
    Sistema->>DB: 2.3 consultarInstituicoesAtivas()
    activate DB
    DB-->>Sistema: 2.4 listaInstituicoes
    deactivate DB
    Sistema-->>Interface: 2.5 instituicoesDisponiveis
    deactivate Sistema
    Interface-->>Usuario: 2.6 exibirInstituicoes()
```

