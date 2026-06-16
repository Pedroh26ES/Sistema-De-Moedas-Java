# DiagramaDeSequencia - RF-02 - Permitir cadastro de alunos

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Aluno,DB: 1. Cadastro de aluno
    Aluno->>Interface: 1.1 preencherDados(nome, email, cpf, rg, endereco, instituicao, curso)
    Interface->>Sistema: 1.2 cadastrarAluno(dados)
    activate Sistema
    Note right of Sistema: Valida dados obrigatorios, documentos e vinculos academicos
    Sistema->>DB: 1.3 verificarEmailCpfRgInstituicaoCurso(dados)
    activate DB
    DB-->>Sistema: 1.4 resultadoValidacao
    alt Dados invalidos ou duplicados
        Sistema-->>Interface: 1.5 cadastroNegado(motivo)
        Interface-->>Aluno: 1.6 exibirErroCadastro
    else Dados validos
        Sistema->>DB: 1.7 salvarAluno(dados)
        DB-->>Sistema: 1.8 alunoSalvo
        Sistema-->>Interface: 1.9 cadastroConcluido
        Interface-->>Aluno: 1.10 confirmarCadastro
    end
    deactivate DB
    deactivate Sistema
```

