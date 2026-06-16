# DiagramaDeSequencia - Aluno - UC-12 a UC-15

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`/`loop`.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-02-aluno.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Aluno,DB: 1. Ver saldo, extrato, notificacoes e cupons
    Aluno->>Interface: 1.1 abrirPainelAluno()
    Interface->>Sistema: 1.2 carregarDashboardAluno(aluno)
    activate Sistema
    Sistema->>DB: 1.3 buscarSaldoExtratoNotificacoesCupons(aluno)
    activate DB
    DB-->>Sistema: 1.4 dadosConsolidados
    deactivate DB
    Sistema-->>Interface: 1.5 dashboardAluno
    deactivate Sistema
    Interface-->>Aluno: 1.6 exibirSaldoExtratoNotificacoesCupons

    Note over Aluno,DB: 2. Filtrar extrato por periodo
    Aluno->>Interface: 2.1 selecionarPeriodo(periodo)
    Interface->>Sistema: 2.2 filtrarExtratoAluno(periodo)
    activate Sistema
    Sistema->>DB: 2.3 consultarTransacoesDoPeriodo(aluno, periodo)
    activate DB
    DB-->>Sistema: 2.4 transacoesFiltradas
    deactivate DB
    alt Existem movimentacoes
        Sistema-->>Interface: 2.5 extratoFiltrado
        Interface-->>Aluno: 2.6 listaDeMovimentacoes
    else Sem movimentacoes
        Sistema-->>Interface: 2.7 extratoVazio
        Interface-->>Aluno: 2.8 "Nenhuma movimentacao no periodo."
    end
    deactivate Sistema

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

    Note over Aluno,DB: 4. Filtrar vantagens disponiveis, adquiridas e metas
    Aluno->>Interface: 4.1 escolherFiltro(filtro)
    Interface->>Sistema: 4.2 classificarVantagens(aluno, filtro)
    activate Sistema
    Sistema->>DB: 4.3 buscarCatalogoSaldoEResgates(aluno)
    activate DB
    DB-->>Sistema: 4.4 dadosParaClassificacao
    deactivate DB
    loop Para cada vantagem
        Sistema->>Sistema: 4.5 compararSaldoCustoEResgates()
    end
    alt Filtro disponiveis
        Sistema-->>Interface: 4.6 vantagensQuePodeResgatar
    else Filtro adquiridas
        Sistema-->>Interface: 4.7 vantagensJaResgatadas
    else Filtro metas
        Sistema-->>Interface: 4.8 vantagensComMoedasFaltantes
    end
    deactivate Sistema
    Interface-->>Aluno: 4.9 listaFiltrada
```
