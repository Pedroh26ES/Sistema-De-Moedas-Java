# DiagramaDeSequencia - UC-23 - Buscar e selecionar aluno

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-03-professor.md)

```mermaid
sequenceDiagram
    actor Professor
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Professor,DB: 4. Buscar e selecionar aluno
    Professor->>Interface: 4.1 pesquisarAluno(termo)
    Interface->>Sistema: 4.2 buscarAlunos(termo)
    activate Sistema
    Sistema->>DB: 4.3 consultarAlunosPorNomeEmailCurso(termo)
    activate DB
    DB-->>Sistema: 4.4 alunosEncontrados
    deactivate DB
    alt Alunos encontrados
        Sistema-->>Interface: 4.5 listaDeAlunos
        Interface-->>Professor: 4.6 alunosSelecionaveis
    else Nenhum aluno encontrado
        Sistema-->>Interface: 4.7 listaVazia
        Interface-->>Professor: 4.8 "Nenhum aluno encontrado."
    end
    deactivate Sistema
```

