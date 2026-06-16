# DiagramaDeSequencia - RF-03 - Manter instituicoes e cursos pre-cadastrados

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Aluno,DB: 1. Selecao de instituicao e curso pre-cadastrados
    Aluno->>Interface: 1.1 abrirFormularioCadastro()
    Interface->>Sistema: 1.2 listarInstituicoes()
    activate Sistema
    Sistema->>DB: 1.3 consultarInstituicoesPreCadastradas()
    activate DB
    DB-->>Sistema: 1.4 instituicoes
    Sistema-->>Interface: 1.5 opcoesInstituicao
    Interface-->>Aluno: 1.6 exibirInstituicoes
    Aluno->>Interface: 1.7 selecionarInstituicao(instituicao)
    Interface->>Sistema: 1.8 listarCursos(instituicao)
    Sistema->>DB: 1.9 consultarCursosDaInstituicao(instituicao)
    DB-->>Sistema: 1.10 cursos
    deactivate DB
    Sistema-->>Interface: 1.11 opcoesCurso
    deactivate Sistema
    Interface-->>Aluno: 1.12 exibirCursosPermitidos
```

