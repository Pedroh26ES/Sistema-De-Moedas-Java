# DiagramaDeSequencia - UC-29 - Editar vantagem

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-04-empresa-parceira.md)

```mermaid
sequenceDiagram
    actor Empresa
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Empresa,DB: 3. Editar vantagem
    Empresa->>Interface: 3.1 alterarDadosDaVantagem(id, dados)
    Interface->>Sistema: 3.2 editarVantagem(empresa, id, dados)
    activate Sistema
    Sistema->>DB: 3.3 consultarVantagemDaEmpresa(empresa, id)
    activate DB
    DB-->>Sistema: 3.4 vantagemEncontrada
    alt Vantagem pertence a empresa
        Sistema->>DB: 3.5 salvarAlteracoes(dados)
        DB-->>Sistema: 3.6 alteracoesSalvas
        Sistema-->>Interface: 3.7 edicaoConcluida
        Interface-->>Empresa: 3.8 vantagemAtualizada
    else Vantagem nao pertence a empresa
        Sistema-->>Interface: 3.9 edicaoBloqueada
        Interface-->>Empresa: 3.10 "Vantagem nao encontrada para esta empresa."
    end
    deactivate DB
    deactivate Sistema
```

