# DiagramaDeSequencia - RF-01 - Identificar produto Valoriza Ae

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant Notificacao as Sistema de Notificacao
    participant Docs as Documentacao

    Note over Usuario,Docs: 1. Identificacao do produto nas interfaces e comunicacoes
    Usuario->>Interface: 1.1 acessarSistema()
    Interface->>Sistema: 1.2 carregarIdentidadeVisual()
    activate Sistema
    Sistema-->>Interface: 1.3 nomeLogoTextosDoProduto
    deactivate Sistema
    Interface-->>Usuario: 1.4 exibirValorizaAe()
    Sistema->>Notificacao: 1.5 montarMensagemComNomeDoProduto(evento)
    Notificacao-->>Usuario: 1.6 emailOuWhatsAppComValorizaAe
    Sistema->>Docs: 1.7 manterNomeNosArtefatos()
    Docs-->>Sistema: 1.8 documentacaoAtualizada
```

