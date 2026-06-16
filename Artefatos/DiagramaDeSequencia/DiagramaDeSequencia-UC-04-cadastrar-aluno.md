# DiagramaDeSequencia - UC-04 - Cadastrar aluno

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-01-conta-cadastro-seguranca.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Usuario,DB: 4. Cadastrar aluno
    Usuario->>Interface: 4.1 preencherDadosAluno(dados)
    Interface->>Sistema: 4.2 cadastrarAluno(dados)
    activate Sistema
    Note right of Sistema: Valida instituicao, curso, email, CPF e RG
    Sistema->>DB: 4.3 verificarInstituicaoCursoEDuplicidade(dados)
    activate DB
    DB-->>Sistema: 4.4 resultadoValidacao
    alt Dados invalidos ou duplicados
        Sistema-->>Interface: 4.5 cadastroRecusado(motivo)
        Interface-->>Usuario: 4.6 mensagemDeErro
    else Dados validos
        Sistema->>DB: 4.7 salvarAluno(dados)
        DB-->>Sistema: 4.8 alunoSalvo
        Sistema-->>Interface: 4.9 cadastroConcluido
        Interface-->>Usuario: 4.10 confirmacaoDeCadastro
    end
    deactivate DB
    deactivate Sistema
```

