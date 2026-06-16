# DiagramaDeSequencia - UC-09 - Solicitar recuperacao de senha

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

    Note over Usuario,EmailJS: 1. Solicitar recuperacao de senha
    Usuario->>Interface: 1.1 solicitarRecuperacao(email)
    Interface->>Sistema: 1.2 gerarTokenRecuperacao(email)
    activate Sistema
    Sistema->>DB: 1.3 buscarUsuarioEInvalidarTokens(email)
    activate DB
    DB-->>Sistema: 1.4 resultadoBusca
    alt Usuario encontrado
        Sistema->>DB: 1.5 salvarTokenRecuperacao(token)
        DB-->>Sistema: 1.6 tokenRegistrado
        Sistema->>EmailJS: 1.7 enviarLinkDeRedefinicao(email, token)
        activate EmailJS
        EmailJS-->>Sistema: 1.8 emailAceito
        deactivate EmailJS
        Sistema-->>Interface: 1.9 instrucaoEnviada
    else Usuario nao encontrado
        Sistema-->>Interface: 1.10 retornoSeguroSemExporConta
    end
    deactivate DB
    deactivate Sistema
    Interface-->>Usuario: 1.11 "Se o email existir, enviaremos instrucoes."
```

