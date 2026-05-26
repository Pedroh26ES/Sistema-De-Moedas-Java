# DiagramaDeComunicacao - release 2-3

Artefato das Releases 2 e 3 do Valoriza Ae.

Este diagrama mostra como os participantes se comunicam quando uma operacao gera saldo, cupom, email, evento de fila e atualizacao de painel.

## Diagrama de comunicacao

```mermaid
flowchart LR
    subgraph Atores["Atores"]
        Professor["Professor"]
        Aluno["Aluno"]
        Empresa["Empresa parceira"]
    end

    subgraph UI["Frontend React"]
        PainelProfessor["Painel Professor"]
        PainelAluno["Painel Aluno"]
        PainelEmpresa["Painel Empresa"]
        Filtros["Filtros de periodo"]
        ModalQR["Modal de QR Code"]
    end

    subgraph API["Backend Quarkus"]
        Controller["ApiController"]
        Sessao["SessaoService"]
        Moedas["MoedaService"]
        Vantagens["VantagemService"]
        Cadastro["CadastroService"]
        Recuperacao["RecuperacaoSenhaService"]
        Email["EmailOutboxGateway"]
        Fila["RabbitMqFilaService"]
        QR["QrCodeService"]
        Cep["ViaCepService"]
    end

    subgraph Persistencia["Banco e auditoria"]
        DB[(Entidades JPA)]
        Outbox[(emails_notificacao)]
        LocalQueue[(eventos_fila_local)]
    end

    subgraph Externos["Servicos externos"]
        EmailJS["EmailJS"]
        RabbitMQ["RabbitMQ"]
        ViaCEP["ViaCEP"]
        ZXing["ZXing"]
    end

    Professor -- "1 envia moedas" --> PainelProfessor
    PainelProfessor -- "2 POST /api/professor/envios" --> Controller
    Controller -- "3 valida perfil" --> Sessao
    Controller -- "4 executa envio" --> Moedas
    Moedas -- "5 debita, credita e registra transacao" --> DB
    Moedas -- "6 notifica aluno e professor" --> Email
    Moedas -- "7 publica MOEDAS_ENVIADAS" --> Fila

    Aluno -- "8 resgata vantagem" --> PainelAluno
    PainelAluno -- "9 POST /api/aluno/resgates" --> Controller
    Controller -- "10 executa resgate" --> Vantagens
    Vantagens -- "11 gera cupom e transacao" --> DB
    Vantagens -- "12 solicita QR Code" --> QR
    QR -- "13 usa biblioteca" --> ZXing
    Vantagens -- "14 publica CUPOM_GERADO" --> Fila
    PainelAluno -- "15 amplia QR Code" --> ModalQR

    Empresa -- "16 valida cupom" --> PainelEmpresa
    PainelEmpresa -- "17 POST /api/empresa/cupons/validar" --> Controller
    Controller -- "18 valida cupom" --> Vantagens
    Vantagens -- "19 marca validado" --> DB
    Vantagens -- "20 publica CUPOM_VALIDADO" --> Fila
    Vantagens -- "21 confirma ao aluno" --> Email

    PainelAluno -- "22 consulta endereco" --> Controller
    Controller -- "23 GET /api/cep" --> Cep
    Cep -- "24 consulta externo" --> ViaCEP

    Email -- "25 registra outbox" --> Outbox
    Email -- "26 envia email real" --> EmailJS
    Fila -- "27 envia evento real" --> RabbitMQ
    Fila -. "28 fallback local em dev" .-> LocalQueue
    Controller -- "29 retorna dashboards" --> PainelProfessor
    Controller -- "30 retorna dashboards" --> PainelAluno
    Controller -- "31 retorna dashboards" --> PainelEmpresa
    PainelAluno --> Filtros
    PainelProfessor --> Filtros
```

## Mensagens principais

- MOEDAS_ENVIADAS: envio confirmado pelo professor.
- CUPOM_GERADO: vantagem resgatada e cupom pendente.
- CUPOM_VALIDADO: atendimento confirmado pela empresa.
- CUPOM_DESATIVADO e CUPOM_REATIVADO: status da vantagem mudou e aluno foi avisado.
