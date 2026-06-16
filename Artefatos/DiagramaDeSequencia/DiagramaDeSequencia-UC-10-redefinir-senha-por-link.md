# DiagramaDeSequencia - UC-10 - Redefinir senha por link

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

    Note over Usuario,DB: 2. Redefinir senha por link
    Usuario->>Interface: 2.1 informarNovaSenha(token, novaSenha)
    Interface->>Sistema: 2.2 redefinirSenha(token, novaSenha)
    activate Sistema
    Sistema->>DB: 2.3 validarTokenAtivo(token)
    activate DB
    DB-->>Sistema: 2.4 statusToken
    alt Token valido e nao expirado
        Sistema->>DB: 2.5 atualizarSenhaEInvalidarToken(novaSenha)
        DB-->>Sistema: 2.6 senhaAtualizada
        Sistema-->>Interface: 2.7 redefinicaoConcluida
        Interface-->>Usuario: 2.8 "Senha alterada com sucesso."
    else Token invalido, expirado ou usado
        Sistema-->>Interface: 2.9 redefinicaoNegada
        Interface-->>Usuario: 2.10 "Link invalido ou expirado."
    end
    deactivate DB
    deactivate Sistema
```

