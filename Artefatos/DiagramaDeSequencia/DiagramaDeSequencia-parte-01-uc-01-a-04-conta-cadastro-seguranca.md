# DiagramaDeSequencia - Conta, cadastro e seguranca - UC-01 a UC-04

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`.

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

    Note over Usuario,Sistema: 3. Sair do sistema
    Usuario->>Interface: 3.1 solicitarLogout()
    Interface->>Sistema: 3.2 encerrarSessao()
    activate Sistema
    Sistema-->>Interface: 3.3 sessaoRemovida
    deactivate Sistema
    Interface-->>Usuario: 3.4 loginExibido

    Note over Usuario,DB: 4. Cadastrar aluno
    Usuario->>Interface: 4.1 preencherDadosAluno(dados)
    Interface->>Sistema: 4.2 cadastrarAluno(dados)
    activate Sistema
    Note right of Sistema: Valida instituicao, curso, email, CPF e RG
    Sistema->>DB: 4.3 verificarInstituicaoCursoEDuplicidade(dados)
    activate DB
    DB-->>Sistema: 4.4 resultadoValidacao
    alt Dados invalidos ou duplicados
        Sistema-->>Interface: 4.5 cadastroRecusado(motivo)
        Interface-->>Usuario: 4.6 mensagemDeErro
    else Dados validos
        Sistema->>DB: 4.7 salvarAluno(dados)
        DB-->>Sistema: 4.8 alunoSalvo
        Sistema-->>Interface: 4.9 cadastroConcluido
        Interface-->>Usuario: 4.10 confirmacaoDeCadastro
    end
    deactivate DB
    deactivate Sistema
```
