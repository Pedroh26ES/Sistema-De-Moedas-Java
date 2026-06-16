# DiagramaDeSequencia - UC-07 - Selecionar curso da instituicao

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

    Note over Usuario,DB: 3. Selecionar curso da instituicao
    Usuario->>Interface: 3.1 selecionarInstituicao(instituicaoId)
    Interface->>Sistema: 3.2 listarCursosDaInstituicao(instituicaoId)
    activate Sistema
    Sistema->>DB: 3.3 consultarCursosVinculados(instituicaoId)
    activate DB
    DB-->>Sistema: 3.4 listaCursos
    deactivate DB
    alt Instituicao possui cursos
        Sistema-->>Interface: 3.5 cursosDisponiveis
        Interface-->>Usuario: 3.6 exibirCursos()
    else Instituicao sem curso cadastrado
        Sistema-->>Interface: 3.7 listaVazia
        Interface-->>Usuario: 3.8 informarSemCursos()
    end
    deactivate Sistema
```

