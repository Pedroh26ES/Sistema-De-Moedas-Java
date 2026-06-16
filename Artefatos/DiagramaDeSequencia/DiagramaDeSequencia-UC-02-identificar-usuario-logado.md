# DiagramaDeSequencia - UC-02 - Identificar usuario logado

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-01-conta-cadastro-seguranca.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Usuario,DB: 2. Identificar usuario logado
    Usuario->>Interface: 2.1 acessaAreaProtegida()
    Interface->>Sistema: 2.2 consultarUsuarioAtual()
    activate Sistema
    Sistema->>DB: 2.3 buscarUsuarioDaSessao()
    activate DB
    DB-->>Sistema: 2.4 usuarioLogado
    deactivate DB
    alt Sessao valida
        Sistema-->>Interface: 2.5 dadosDoPerfil
        Interface-->>Usuario: 2.6 telaPermitida
    else Sessao ausente ou expirada
        Sistema-->>Interface: 2.7 acessoNaoAutenticado
        Interface-->>Usuario: 2.8 redirecionarParaLogin()
    end
    deactivate Sistema
```

