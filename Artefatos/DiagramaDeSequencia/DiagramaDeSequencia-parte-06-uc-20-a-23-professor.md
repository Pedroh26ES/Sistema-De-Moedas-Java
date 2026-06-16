# DiagramaDeSequencia - Professor - UC-20 a UC-23

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados e fragmentos `alt`.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-03-professor.md)

```mermaid
sequenceDiagram
    actor Professor
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Professor,DB: 1. Ver cota, alunos, extrato e notificacoes
    Professor->>Interface: 1.1 abrirPainelProfessor()
    Interface->>Sistema: 1.2 carregarDashboardProfessor(professor)
    activate Sistema
    Sistema->>DB: 1.3 buscarCotaAlunosExtratoNotificacoes(professor)
    activate DB
    DB-->>Sistema: 1.4 dadosProfessor
    deactivate DB
    Sistema-->>Interface: 1.5 dashboardProfessor
    deactivate Sistema
    Interface-->>Professor: 1.6 exibirCotaAlunosExtratoNotificacoes

    Note over Professor,DB: 2. Filtrar extrato por periodo
    Professor->>Interface: 2.1 selecionarPeriodo(periodo)
    Interface->>Sistema: 2.2 filtrarExtratoProfessor(periodo)
    activate Sistema
    Sistema->>DB: 2.3 consultarEnviosFiltrados(professor, periodo)
    activate DB
    DB-->>Sistema: 2.4 enviosDoPeriodo
    deactivate DB
    Sistema-->>Interface: 2.5 extratoFiltrado
    deactivate Sistema
    Interface-->>Professor: 2.6 extratoAtualizado

    Note over Sistema,DB: 3. Creditar cota semestral
    Sistema->>Sistema: 3.1 identificarSemestreAtual()
    Sistema->>DB: 3.2 buscarProfessores()
    activate DB
    DB-->>Sistema: 3.3 professoresEncontrados
    loop Para cada professor
        Sistema->>DB: 3.4 atualizarCotaSemestral(professor)
        DB-->>Sistema: 3.5 cotaAtualizada
        Sistema->>DB: 3.6 registrarCreditoSemestral(professor)
        DB-->>Sistema: 3.7 creditoRegistrado
    end
    deactivate DB

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
