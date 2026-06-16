# DiagramaDeSequencia - RF-04 - Manter professores pre-cadastrados

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    actor Professor
    participant Interface

    Note over Sistema,DB: 1. Carga e manutencao de professores pre-cadastrados
    Sistema->>DB: 1.1 consultarProfessoresExistentes()
    activate DB
    DB-->>Sistema: 1.2 professoresAtuais
    alt Professor inicial ausente
        Sistema->>DB: 1.3 salvarProfessor(nome, cpf, departamento, instituicao)
        DB-->>Sistema: 1.4 professorPreCadastrado
    else Professor ja cadastrado
        DB-->>Sistema: 1.5 cadastroMantido
    end
    deactivate DB
    Professor->>Interface: 1.6 entrarComEmailInstitucional()
    Interface->>Sistema: 1.7 autenticarProfessor(email, senha)
    Sistema->>DB: 1.8 buscarProfessorPorEmail(email)
    activate DB
    DB-->>Sistema: 1.9 professorComInstituicao
    deactivate DB
    Sistema-->>Interface: 1.10 acessoProfessor
    Interface-->>Professor: 1.11 painelProfessor
```

