# DiagramaDeSequencia - Integracoes e rastreabilidade - UC-41

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmento `loop`.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-05-integracoes-rastreabilidade.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Usuario,DB: 1. Manter rastreabilidade em extratos e notificacoes
    Sistema->>DB: 1.1 registrarTransacaoDeNegocio(transacao)
    activate DB
    DB-->>Sistema: 1.2 transacaoSalva
    Sistema->>DB: 1.3 registrarNotificacao(notificacao)
    DB-->>Sistema: 1.4 notificacaoSalva
    Sistema->>DB: 1.5 registrarEventoLocalSeNecessario(evento)
    DB-->>Sistema: 1.6 eventoLocalSalvo
    deactivate DB

    Usuario->>Interface: 1.7 consultarPainelOuExtrato(filtro)
    Interface->>Sistema: 1.8 carregarHistoricoConsolidado(usuario, filtro)
    activate Sistema
    Sistema->>DB: 1.9 buscarTransacoesNotificacoesEventos(usuario, filtro)
    activate DB
    DB-->>Sistema: 1.10 historicoConsolidado
    deactivate DB
    loop Para cada registro historico
        Sistema->>Sistema: 1.11 classificarOrigemEStatus(registro)
    end
    Sistema-->>Interface: 1.12 dadosRastreaveis
    deactivate Sistema
    Interface-->>Usuario: 1.13 extratoNotificacoesEventosExibidos
```
