# DiagramaDeSequencia - UC-05 - Cadastrar empresa parceira

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-01-conta-cadastro-seguranca.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant ViaCEP

    Note over Usuario,DB: 1. Cadastrar empresa parceira
    Usuario->>Interface: 1.1 preencherDadosEmpresa(dados)
    Interface->>Sistema: 1.2 cadastrarEmpresa(dados)
    activate Sistema
    Note right of Sistema: Valida email, documento, contato e dados obrigatorios
    Sistema->>DB: 1.3 verificarDuplicidadeEmpresa(dados)
    activate DB
    DB-->>Sistema: 1.4 resultadoValidacao
    alt Dados invalidos ou duplicados
        Sistema-->>Interface: 1.5 cadastroRecusado(motivo)
        Interface-->>Usuario: 1.6 mensagemDeErro
    else Dados validos
        Sistema->>DB: 1.7 salvarEmpresaParceira(dados)
        DB-->>Sistema: 1.8 empresaSalva
        Sistema-->>Interface: 1.9 cadastroConcluido
        Interface-->>Usuario: 1.10 confirmacaoDeCadastro
    end
    deactivate DB
    deactivate Sistema
```

