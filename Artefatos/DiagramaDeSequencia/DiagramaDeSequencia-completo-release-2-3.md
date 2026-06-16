# DiagramaDeSequencia completo - release 2-3

Artefato das Releases 2 e 3 do Valoriza Ae.

Este arquivo mantem uma visao consolidada no modelo do gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`, `loop` e `opt`.

A versao dividida por requisito funcional fica em `DiagramaDeSequencia-release-2-3.md`.

## Diagrama de sequencia completo

```mermaid
sequenceDiagram
    actor Aluno
    actor Professor
    actor Empresa
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant ViaCEP
    participant QR as Gerador de QR Code
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Aluno,ViaCEP: 1. Conta, cadastro e seguranca
    Aluno->>Interface: 1.1 solicitarCadastroAluno(dados, cep)
    activate Interface
    Interface->>Sistema: 1.2 carregarInstituicoesECursos()
    activate Sistema
    Sistema->>DB: 1.3 consultarInstituicoesECursos()
    activate DB
    DB-->>Sistema: 1.4 instituicoesECursos
    deactivate DB
    Sistema-->>Interface: 1.5 opcoesDeCadastro
    Interface->>Sistema: 1.6 consultarCep(cep)
    Sistema->>ViaCEP: 1.7 buscarEndereco(cep)
    activate ViaCEP
    ViaCEP-->>Sistema: 1.8 enderecoEncontrado
    deactivate ViaCEP
    Sistema-->>Interface: 1.9 enderecoFormatado
    Interface->>Sistema: 1.10 cadastrarAluno(dados)
    Sistema->>DB: 1.11 validarDuplicidadeInstituicaoCurso(dados)
    activate DB
    DB-->>Sistema: 1.12 resultadoValidacao
    alt Dados invalidos ou duplicados
        Sistema-->>Interface: 1.13 cadastroNegado(motivo)
        Interface-->>Aluno: 1.14 "Cadastro nao concluido."
    else Dados validos
        Sistema->>DB: 1.15 salvarAluno(dados)
        DB-->>Sistema: 1.16 alunoSalvo
        Sistema-->>Interface: 1.17 cadastroConcluido
        Interface-->>Aluno: 1.18 confirmacaoDeCadastro
    end
    deactivate DB
    deactivate Sistema
    deactivate Interface

    Note over Professor,Fila: 2. Envio de moedas pelo professor
    Professor->>Interface: 2.1 enviarMoedas(aluno, valor, motivo)
    activate Interface
    Interface->>Sistema: 2.2 registrarEnvioMoedas(professor, aluno, valor, motivo)
    activate Sistema
    Note right of Sistema: Valida professor, aluno, saldo da cota e justificativa
    Sistema->>DB: 2.3 consultarProfessorAlunoESaldo()
    activate DB
    DB-->>Sistema: 2.4 dadosParaEnvio
    alt Saldo insuficiente ou motivo ausente
        Sistema-->>Interface: 2.5 envioNegado(motivo)
        Interface-->>Professor: 2.6 "Revise saldo, valor ou justificativa."
    else Envio permitido
        Sistema->>DB: 2.7 debitarProfessorCreditarAluno()
        Sistema->>DB: 2.8 registrarTransacaoEnvio()
        DB-->>Sistema: 2.9 transacaoRegistrada
        Sistema->>Notificacao: 2.10 notificarAlunoEProfessor()
        activate Notificacao
        Notificacao-->>Sistema: 2.10.1 notificacoesRegistradas
        deactivate Notificacao
        Sistema->>Fila: 2.11 publicarMOEDAS_ENVIADAS()
        activate Fila
        Fila-->>Sistema: 2.11.1 eventoPublicado
        deactivate Fila
        Sistema-->>Interface: 2.12 envioConfirmado
        Interface-->>Professor: 2.13 cotaExtratoAtualizados
    end
    deactivate DB
    deactivate Sistema
    deactivate Interface

    Note over Aluno,Fila: 3. Resgate de vantagem pelo aluno
    Aluno->>Interface: 3.1 solicitarResgate(vantagem)
    activate Interface
    Interface->>Sistema: 3.2 resgatarVantagem(aluno, vantagem)
    activate Sistema
    Note right of Sistema: Valida saldo, vantagem ativa e compra duplicada
    Sistema->>DB: 3.3 consultarAlunoVantagemCupons()
    activate DB
    DB-->>Sistema: 3.4 dadosDoResgate
    alt Saldo insuficiente ou cupom duplicado
        Sistema-->>Interface: 3.5 resgateNegado(motivo)
        Interface-->>Aluno: 3.6 "Nao foi possivel resgatar."
    else Resgate permitido
        Sistema->>DB: 3.7 debitarSaldoECriarCupom()
        DB-->>Sistema: 3.8 cupomPendenteSalvo
        Sistema->>QR: 3.9 gerarQrCode(cupom)
        activate QR
        QR-->>Sistema: 3.10 qrCodeGerado
        deactivate QR
        Sistema->>Notificacao: 3.11 enviarCupomAlunoParceiro()
        activate Notificacao
        Notificacao-->>Sistema: 3.11.1 cupomEnviado
        deactivate Notificacao
        Sistema->>Fila: 3.12 publicarCUPOM_GERADO()
        activate Fila
        Fila-->>Sistema: 3.12.1 eventoPublicado
        deactivate Fila
        Sistema-->>Interface: 3.13 codigoStatusQrCode
        Interface-->>Aluno: 3.14 cupomPendenteExibido
    end
    deactivate DB
    deactivate Sistema
    deactivate Interface

    Note over Empresa,Fila: 4. Gestao de vantagem e validacao de cupom
    Empresa->>Interface: 4.1 consultarCupom(codigo)
    activate Interface
    Interface->>Sistema: 4.2 buscarCupomDaEmpresa(empresa, codigo)
    activate Sistema
    Sistema->>DB: 4.3 consultarCupomEVantagem(codigo)
    activate DB
    DB-->>Sistema: 4.4 dadosCupom
    alt Cupom pendente, ativo e da empresa
        Sistema-->>Interface: 4.5 cupomDisponivelParaValidacao
        Empresa->>Interface: 4.6 confirmarValidacao(codigo)
        Interface->>Sistema: 4.7 validarCupom(empresa, codigo)
        Sistema->>DB: 4.8 marcarCupomValidado(codigo)
        DB-->>Sistema: 4.9 validacaoSalva
        Sistema->>Notificacao: 4.10 notificarAlunoCupomValidado()
        activate Notificacao
        Notificacao-->>Sistema: 4.10.1 notificacaoRegistrada
        deactivate Notificacao
        Sistema->>Fila: 4.11 publicarCUPOM_VALIDADO()
        activate Fila
        Fila-->>Sistema: 4.11.1 eventoPublicado
        deactivate Fila
        Sistema-->>Interface: 4.12 validacaoConcluida
        Interface-->>Empresa: 4.13 atendimentoConfirmado
    else Cupom usado, pausado ou de outra empresa
        Sistema-->>Interface: 4.14 validacaoBloqueada(motivo)
        Interface-->>Empresa: 4.15 motivoDoBloqueio
    end
    deactivate DB
    deactivate Sistema
    deactivate Interface

    Note over Empresa,Fila: 5. Alteracao de vantagem e notificacoes automaticas
    Empresa->>Interface: 5.1 alterarStatusVantagem(vantagem, status)
    activate Interface
    Interface->>Sistema: 5.2 pausarOuReativarVantagem(empresa, vantagem, status)
    activate Sistema
    Sistema->>DB: 5.3 validarVantagemEConsultarCuponsPendentes()
    activate DB
    DB-->>Sistema: 5.4 vantagemECuponsPendentes
    Sistema->>DB: 5.5 atualizarStatusDaVantagem(status)
    DB-->>Sistema: 5.6 statusAtualizado
    deactivate DB
    loop Para cada aluno com cupom pendente
        Sistema->>Notificacao: 5.7 avisarAlunoAfetado(aluno, cupom, status)
        activate Notificacao
        Notificacao-->>Sistema: 5.7.1 avisoRegistrado
        deactivate Notificacao
    end
    alt Vantagem pausada
        Sistema->>Fila: 5.8 publicarCUPOM_DESATIVADO()
        activate Fila
        Fila-->>Sistema: 5.8.1 eventoPublicado
        deactivate Fila
    else Vantagem reativada
        Sistema->>Fila: 5.9 publicarCUPOM_REATIVADO()
        activate Fila
        Fila-->>Sistema: 5.9.1 eventoPublicado
        deactivate Fila
    end
    opt RabbitMQ indisponivel em desenvolvimento
        Sistema->>DB: 5.10 persistirEventoLocal()
        activate DB
        DB-->>Sistema: 5.10.1 eventoLocalRegistrado
        deactivate DB
    end
    Sistema-->>Interface: 5.11 catalogoAtualizado
    deactivate Sistema
    Interface-->>Empresa: 5.12 novoStatusExibido
    deactivate Interface
```

## Observacao

O diagrama completo foi mantido como visao geral. Para avaliacao detalhada por requisito funcional, use os arquivos indicados no indice `DiagramaDeSequencia-release-2-3.md`.
