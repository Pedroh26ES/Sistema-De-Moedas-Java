# DiagramaDeSequencia - release 2-3

Artefato das Releases 2 e 3 do Valoriza Ae.

Este arquivo consolida os principais fluxos de sequencia implementados nas Releases 2 e 3.

## Sequencia - login e acesso por perfil

```mermaid
sequenceDiagram
    actor Usuario
    participant React as Frontend React
    participant API as ApiController
    participant Sessao as SessaoService
    participant Senha as SenhaService
    participant DB as Banco JPA

    Usuario->>React: informa email e senha
    React->>API: POST /api/login
    API->>Sessao: login(email, senha)
    Sessao->>DB: busca usuario por email
    DB-->>Sessao: usuario ativo com perfil
    Sessao->>Senha: confere senha com hash salvo
    alt credenciais validas
        Sessao-->>API: sessao com token e perfil
        API-->>React: cookie + SessionDto
        React->>API: GET /api/me
        API->>Sessao: porToken(token)
        Sessao-->>API: usuario logado
        API-->>React: perfil do usuario
        React-->>Usuario: redireciona para painel do perfil
    else credenciais invalidas
        API-->>React: erro de login
        React-->>Usuario: mostra mensagem de acesso negado
    end
```

---

## Sequencia - cadastro de aluno com instituicao, curso e ViaCEP

```mermaid
sequenceDiagram
    actor Aluno
    participant React as Frontend React
    participant API as ApiController
    participant Cadastro as CadastroService
    participant ViaCep as ViaCepService
    participant ViaCEP as API ViaCEP
    participant Senha as SenhaService
    participant DB as Banco JPA

    Aluno->>React: abre cadastro de aluno
    React->>API: GET /api/instituicoes
    API->>Cadastro: instituicoesDisponiveis()
    Cadastro->>DB: consulta instituicoes e cursos pre-cadastrados
    DB-->>React: lista de instituicoes e cursos

    Aluno->>React: informa CEP
    React->>API: GET /api/cep/{cep}
    API->>ViaCep: consultar(cep)
    alt CEP com 8 digitos e existente
        ViaCep->>ViaCEP: GET /ws/{cep}/json
        ViaCEP-->>ViaCep: endereco
        ViaCep-->>React: endereco formatado
    else CEP invalido ou nao encontrado
        ViaCep-->>React: mensagem de erro
    end

    Aluno->>React: envia dados completos
    React->>API: POST /api/alunos
    API->>Cadastro: cadastrarAluno(...)
    Cadastro->>DB: valida email unico, CPF unico e instituicao
    Cadastro->>DB: valida curso pertencente a instituicao
    Cadastro->>Senha: gerarHash(senha)
    Cadastro->>DB: salva Usuario + Aluno
    API-->>React: aluno criado
    React-->>Aluno: cadastro concluido
```

---

## Sequencia - recuperacao de senha por EmailJS

```mermaid
sequenceDiagram
    actor Usuario
    participant React as Frontend React
    participant API as ApiController
    participant Recuperacao as RecuperacaoSenhaService
    participant Senha as SenhaService
    participant Email as EmailOutboxGateway
    participant EmailJS as EmailJS
    participant DB as Banco JPA

    Usuario->>React: informa email em Esqueci minha senha
    React->>API: POST /api/senha/esqueci
    API->>Recuperacao: solicitar(email)
    Recuperacao->>DB: busca usuario por email
    alt email cadastrado
        Recuperacao->>DB: invalida tokens pendentes
        Recuperacao->>Senha: gera hash do token
        Recuperacao->>DB: salva token com validade
        Recuperacao->>Email: enviar link de redefinicao
        Email->>DB: registra email_notificacao
        Email->>EmailJS: envia email real
        API-->>React: instrucao enviada
    else email nao cadastrado
        API-->>React: erro de email nao encontrado
    end

    Usuario->>React: abre link recebido e informa nova senha
    React->>API: POST /api/senha/redefinir
    API->>Recuperacao: redefinir(token, novaSenha)
    Recuperacao->>DB: busca token ativo
    alt token valido e nao expirado
        Recuperacao->>Senha: gerarHash(novaSenha)
        Recuperacao->>DB: atualiza senha e invalida token
        API-->>React: senha alterada
    else token invalido
        API-->>React: link invalido ou expirado
    end
```

---

## Sequencia - envio de moedas pelo professor

```mermaid
sequenceDiagram
    actor Professor
    actor Aluno
    participant React as Frontend React
    participant API as ApiController
    participant Sessao as SessaoService
    participant Moedas as MoedaService
    participant Email as EmailOutboxGateway
    participant Fila as RabbitMqFilaService
    participant MQ as RabbitMQ
    participant DB as Banco JPA

    Professor->>React: escolhe aluno, valor e justificativa
    React->>API: POST /api/professor/envios
    API->>Sessao: exigir(token, PROFESSOR)
    Sessao-->>API: professor autenticado
    API->>Moedas: enviarMoedas(professorId, alunoId, valor, motivo)
    Moedas->>DB: busca professor e aluno
    Moedas->>Moedas: valida valor maior que zero
    Moedas->>Moedas: valida justificativa obrigatoria
    Moedas->>Moedas: valida saldo suficiente do professor
    alt dados validos
        Moedas->>DB: debita professor e credita aluno
        Moedas->>DB: cria transacao ENVIO_MOEDAS
        Moedas->>Email: email para aluno e professor
        Email->>DB: registra notificacoes internas
        Email->>EmailJS: envia emails reais quando permitido
        Moedas->>Fila: publicar MOEDAS_ENVIADAS
        alt RabbitMQ disponivel
            Fila->>MQ: envia evento para fila
        else fallback local habilitado
            Fila->>DB: salva evento_fila_local
        end
        API-->>React: envio confirmado
        React-->>Professor: atualiza cota, extrato e notificacoes
        Email-->>Aluno: aviso de moedas recebidas
    else regra violada
        API-->>React: mensagem de erro
    end
```

---

## Sequencia - resgate de vantagem com cupom e QR Code

```mermaid
sequenceDiagram
    actor Aluno
    actor Empresa
    participant React as Frontend React
    participant API as ApiController
    participant Sessao as SessaoService
    participant Vantagens as VantagemService
    participant QR as QrCodeService
    participant Email as EmailOutboxGateway
    participant Fila as RabbitMqFilaService
    participant DB as Banco JPA

    Aluno->>React: seleciona vantagem no catalogo
    React->>API: POST /api/aluno/resgates
    API->>Sessao: exigir(token, ALUNO)
    API->>Vantagens: resgatar(alunoId, vantagemId)
    Vantagens->>DB: busca aluno e vantagem ativa
    Vantagens->>DB: verifica resgate anterior da mesma vantagem
    Vantagens->>Vantagens: valida saldo suficiente
    alt pode resgatar
        Vantagens->>Vantagens: gera codigo unico do cupom
        Vantagens->>DB: debita saldo do aluno
        Vantagens->>DB: cria transacao RESGATE_VANTAGEM pendente
        Vantagens->>Email: envia cupom ao aluno e aviso a empresa
        Email->>DB: registra emails_notificacao
        Vantagens->>Fila: publica CUPOM_GERADO
        API-->>React: codigo e url do QR Code
        React->>API: GET /api/cupons/{codigo}/qrcode
        API->>QR: gerarPng(url de validacao)
        QR-->>React: PNG do QR Code
        React-->>Aluno: mostra cupom pendente e QR Code ampliavel
        Email-->>Empresa: aviso de novo cupom para validar
    else sem saldo ou duplicado
        API-->>React: resgate negado
        React-->>Aluno: informa motivo
    end
```

---

## Sequencia - validacao de cupom pela empresa

```mermaid
sequenceDiagram
    actor Aluno
    actor Empresa
    participant React as Frontend React
    participant API as ApiController
    participant Sessao as SessaoService
    participant Vantagens as VantagemService
    participant Email as EmailOutboxGateway
    participant Fila as RabbitMqFilaService
    participant DB as Banco JPA

    Aluno->>Empresa: apresenta codigo ou QR Code
    Empresa->>React: abre painel com cupom preenchido
    React->>API: POST /api/empresa/cupons/validar
    API->>Sessao: exigir(token, EMPRESA)
    API->>Vantagens: validarCupom(empresaId, codigo)
    Vantagens->>DB: busca transacao pelo codigo e empresa
    alt cupom pertence a empresa, esta pendente e vantagem ativa
        Vantagens->>DB: marca cupom_validado e validado_em
        Vantagens->>Email: envia confirmacao ao aluno
        Email->>DB: registra notificacao interna
        Vantagens->>Fila: publica CUPOM_VALIDADO
        API-->>React: cupom validado
        React-->>Empresa: atendimento concluido
    else cupom inexistente, usado, pausado ou de outra empresa
        API-->>React: validacao negada
        React-->>Empresa: mostra motivo
    end
```

---

## Sequencia - gestao de vantagem pela empresa

```mermaid
sequenceDiagram
    actor Empresa
    actor Aluno
    participant React as Frontend React
    participant API as ApiController
    participant Sessao as SessaoService
    participant Vantagens as VantagemService
    participant Email as EmailOutboxGateway
    participant Fila as RabbitMqFilaService
    participant DB as Banco JPA

    Empresa->>React: cria ou edita vantagem
    React->>API: POST/PUT /api/empresa/vantagens
    API->>Sessao: exigir(token, EMPRESA)
    API->>Vantagens: cadastrar ou atualizar
    Vantagens->>Vantagens: valida titulo, descricao, foto e custo
    Vantagens->>DB: salva vantagem vinculada a empresa
    API-->>React: catalogo atualizado

    Empresa->>React: pausa ou reativa vantagem
    React->>API: PUT /api/empresa/vantagens/{id}/status
    API->>Vantagens: alterarStatus(empresaId, id, ativa)
    Vantagens->>DB: confirma que a vantagem pertence a empresa
    Vantagens->>DB: atualiza status
    Vantagens->>DB: localiza cupons pendentes da vantagem
    alt possui cupons pendentes
        Vantagens->>Email: notifica alunos afetados
        Vantagens->>Fila: publica CUPOM_DESATIVADO ou CUPOM_REATIVADO
        Email-->>Aluno: aviso sobre status do cupom
    end
    API-->>React: status atualizado

    Empresa->>React: solicita exclusao
    React->>API: DELETE /api/empresa/vantagens/{id}
    API->>Vantagens: excluir(empresaId, id)
    Vantagens->>DB: verifica historico de transacoes
    alt sem historico
        Vantagens->>DB: remove vantagem
        API-->>React: exclusao concluida
    else possui cupom ou resgate
        API-->>React: exclusao bloqueada; pausar vantagem
    end
```

---

## Sequencia - dashboards, extratos e filtros por periodo

```mermaid
sequenceDiagram
    actor Usuario
    participant React as Frontend React
    participant API as ApiController
    participant Sessao as SessaoService
    participant Services as AlunoService/MoedaService/VantagemService
    participant EmailRepo as EmailNotificacaoRepository
    participant DB as Banco JPA

    Usuario->>React: abre painel do perfil
    React->>API: GET /api/aluno/dashboard ou /api/professor/dashboard ou /api/empresa/dashboard
    API->>Sessao: exigir token e perfil correto
    API->>Services: busca dados do dashboard
    Services->>DB: consulta saldo, extrato, catalogo, resgates e cupons
    API->>EmailRepo: busca notificacoes do email do usuario
    EmailRepo->>DB: consulta emails_notificacao
    API-->>React: DTO completo do painel
    React->>React: aplica filtros Dia, Semana, Mes, Ano ou Todos
    React-->>Usuario: mostra dados filtrados e status dos cupons
```
