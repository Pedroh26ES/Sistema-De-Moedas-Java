# DiagramaDeSequencia - Conta, cadastro e seguranca - UC-05 a UC-08

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`.

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

    Note over Usuario,DB: 2. Selecionar instituicao pre-cadastrada
    Usuario->>Interface: 2.1 abrirCadastroAluno()
    Interface->>Sistema: 2.2 listarInstituicoes()
    activate Sistema
    Sistema->>DB: 2.3 consultarInstituicoesAtivas()
    activate DB
    DB-->>Sistema: 2.4 listaInstituicoes
    deactivate DB
    Sistema-->>Interface: 2.5 instituicoesDisponiveis
    deactivate Sistema
    Interface-->>Usuario: 2.6 exibirInstituicoes()

    Note over Usuario,DB: 3. Selecionar curso da instituicao
    Usuario->>Interface: 3.1 selecionarInstituicao(instituicaoId)
    Interface->>Sistema: 3.2 listarCursosDaInstituicao(instituicaoId)
    activate Sistema
    Sistema->>DB: 3.3 consultarCursosVinculados(instituicaoId)
    activate DB
    DB-->>Sistema: 3.4 listaCursos
    deactivate DB
    alt Instituicao possui cursos
        Sistema-->>Interface: 3.5 cursosDisponiveis
        Interface-->>Usuario: 3.6 exibirCursos()
    else Instituicao sem curso cadastrado
        Sistema-->>Interface: 3.7 listaVazia
        Interface-->>Usuario: 3.8 informarSemCursos()
    end
    deactivate Sistema

    Note over Usuario,ViaCEP: 4. Consultar endereco por CEP
    Usuario->>Interface: 4.1 informarCep(cep)
    Interface->>Sistema: 4.2 consultarCep(cep)
    activate Sistema
    Sistema->>ViaCEP: 4.3 buscarEndereco(cep)
    activate ViaCEP
    ViaCEP-->>Sistema: 4.4 respostaViaCep
    deactivate ViaCEP
    alt CEP encontrado
        Sistema-->>Interface: 4.5 enderecoFormatado
        Interface-->>Usuario: 4.6 preencherEndereco(endereco)
    else CEP invalido ou nao encontrado
        Sistema-->>Interface: 4.7 erroConsultaCep
        Interface-->>Usuario: 4.8 informarCepInvalido()
    end
    deactivate Sistema
```
