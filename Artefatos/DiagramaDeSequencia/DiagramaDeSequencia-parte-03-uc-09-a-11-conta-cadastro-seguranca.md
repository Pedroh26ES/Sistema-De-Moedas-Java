# DiagramaDeSequencia - Conta, cadastro e seguranca - UC-09 a UC-11

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`.

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
