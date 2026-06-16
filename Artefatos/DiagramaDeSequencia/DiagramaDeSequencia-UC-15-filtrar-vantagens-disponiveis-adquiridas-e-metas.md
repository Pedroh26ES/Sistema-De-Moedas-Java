# DiagramaDeSequencia - UC-15 - Filtrar vantagens disponiveis, adquiridas e metas

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-02-aluno.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

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

