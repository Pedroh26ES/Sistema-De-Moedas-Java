# DiagramaDeSequencia - Aluno - UC-16 a UC-19

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`/`opt`.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-02-aluno.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant QR as Gerador de QR Code
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Aluno,Fila: 1. Resgatar vantagem
    Aluno->>Interface: 1.1 solicitarResgate(vantagem)
    Interface->>Sistema: 1.2 resgatarVantagem(aluno, vantagem)
    activate Sistema
    Note right of Sistema: Valida saldo, status da vantagem e duplicidade
    Sistema->>DB: 1.3 consultarAlunoVantagemECupons(aluno, vantagem)
    activate DB
    DB-->>Sistema: 1.4 dadosDoResgate
    alt Resgate nao permitido
        Sistema-->>Interface: 1.5 resgateNegado(motivo)
        Interface-->>Aluno: 1.6 "Nao foi possivel resgatar a vantagem."
    else Resgate permitido
        Sistema->>DB: 1.7 debitarSaldoECriarCupom(aluno, vantagem)
        DB-->>Sistema: 1.8 cupomPendenteSalvo
        Sistema->>QR: 1.9 gerarQrCode(cupom)
        activate QR
        QR-->>Sistema: 1.10 qrCodeGerado
        deactivate QR
        Sistema->>Notificacao: 1.11 notificarAlunoEEmpresa(cupom)
        Sistema->>Fila: 1.12 publicarCUPOM_GERADO(cupom)
        Sistema-->>Interface: 1.13 codigoStatusQrCode
        Interface-->>Aluno: 1.14 cupomPendenteExibido
    end
    deactivate DB
    deactivate Sistema

    Note over Aluno,DB: 2. Impedir resgate duplicado da mesma vantagem
    Aluno->>Interface: 2.1 tentarResgatarVantagemJaComprada(vantagem)
    Interface->>Sistema: 2.2 validarDuplicidade(aluno, vantagem)
    activate Sistema
    Sistema->>DB: 2.3 buscarCupomAtivoDaVantagem(aluno, vantagem)
    activate DB
    DB-->>Sistema: 2.4 cupomExistente
    deactivate DB
    alt Cupom pendente ou ativo encontrado
        Sistema-->>Interface: 2.5 duplicidadeBloqueada
        Interface-->>Aluno: 2.6 "Voce ja possui cupom para esta vantagem."
    else Nenhum cupom ativo
        Sistema-->>Interface: 2.7 resgatePodeContinuar
    end
    deactivate Sistema

    Note over Aluno,QR: 3. Ver cupom, status e QR Code
    Aluno->>Interface: 3.1 abrirMeusCupons()
    Interface->>Sistema: 3.2 listarCupons(aluno)
    activate Sistema
    Sistema->>DB: 3.3 consultarCuponsDoAluno(aluno)
    activate DB
    DB-->>Sistema: 3.4 cuponsEStatus
    deactivate DB
    Sistema-->>Interface: 3.5 listaDeCupons
    deactivate Sistema
    Interface->>QR: 3.6 carregarQrCode(codigoCupom)
    activate QR
    QR-->>Interface: 3.7 imagemQrCode
    deactivate QR
    Interface-->>Aluno: 3.8 cupomStatusQrCodeExibidos

    Note over Aluno,QR: 4. Ampliar QR Code do cupom
    Aluno->>Interface: 4.1 selecionarAmpliarQrCode(codigoCupom)
    Interface->>QR: 4.2 gerarImagemAmpliada(codigoCupom)
    activate QR
    QR-->>Interface: 4.3 qrCodeAmpliado
    deactivate QR
    opt Cupom ainda pendente
        Interface-->>Aluno: 4.4 modalComQrCodeEInstrucao
    end
```
