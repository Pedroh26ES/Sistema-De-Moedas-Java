# DiagramaDeSequencia - UC-03 - Sair do sistema

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-01-conta-cadastro-seguranca.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Usuario,Sistema: 3. Sair do sistema
    Usuario->>Interface: 3.1 solicitarLogout()
    Interface->>Sistema: 3.2 encerrarSessao()
    activate Sistema
    Sistema-->>Interface: 3.3 sessaoRemovida
    deactivate Sistema
    Interface-->>Usuario: 3.4 loginExibido
```

