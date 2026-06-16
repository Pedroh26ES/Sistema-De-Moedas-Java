# DiagramaDeSequencia - UC-11 - Bloquear acesso fora do perfil

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-01-conta-cadastro-seguranca.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant EmailJS

    Note over Usuario,Sistema: 3. Bloquear acesso fora do perfil
    Usuario->>Interface: 3.1 acessarRecursoDePerfil(perfilSolicitado)
    Interface->>Sistema: 3.2 validarPerfil(perfilSolicitado)
    activate Sistema
    Note right of Sistema: Compara perfil da sessao com perfil exigido
    alt Perfil permitido
        Sistema-->>Interface: 3.3 acessoLiberado
        Interface-->>Usuario: 3.4 recursoExibido
    else Perfil nao autorizado
        Sistema-->>Interface: 3.5 acessoBloqueado
        Interface-->>Usuario: 3.6 "Acesso nao permitido para este perfil."
    end
    deactivate Sistema
```

