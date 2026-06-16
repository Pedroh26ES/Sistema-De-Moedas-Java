# DiagramaDeSequencia - UC-01 - Entrar no sistema

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-01-conta-cadastro-seguranca.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Usuario,DB: 1. Entrar no sistema
    Usuario->>Interface: 1.1 informarCredenciais(email, senha)
    Interface->>Sistema: 1.2 validarCredenciais(email, senha)
    activate Sistema
    Note right of Sistema: Valida senha, perfil e status ativo
    Sistema->>DB: 1.3 buscarUsuarioPorEmail(email)
    activate DB
    DB-->>Sistema: 1.4 dadosUsuario
    deactivate DB
    alt Credenciais invalidas ou usuario inativo
        Sistema-->>Interface: 1.5 acessoNegado
        Interface-->>Usuario: 1.6 "Email ou senha invalidos."
    else Credenciais validas
        Sistema-->>Interface: 1.7 sessaoCriada(perfil)
        Interface-->>Usuario: 1.8 painelDoPerfil
    end
    deactivate Sistema
```

