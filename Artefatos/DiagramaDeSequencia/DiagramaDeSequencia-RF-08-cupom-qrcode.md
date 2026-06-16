# DiagramaDeSequencia - RF-08 - Gerar cupom unico e QR Code

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Aluno
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant QR as Gerador de QR Code

    Note over Aluno,QR: 1. Geracao de cupom unico e QR Code
    Aluno->>Interface: 1.1 concluirResgate(vantagem)
    Interface->>Sistema: 1.2 gerarCupomDoResgate(aluno, vantagem)
    activate Sistema
    Sistema->>Sistema: 1.3 criarCodigoUnicoCupom()
    Sistema->>DB: 1.4 salvarCupomPendente(codigo, aluno, vantagem)
    activate DB
    DB-->>Sistema: 1.5 cupomSalvo
    deactivate DB
    Sistema->>QR: 1.6 gerarQrCode(urlValidacaoCupom)
    activate QR
    QR-->>Sistema: 1.7 imagemQrCode
    deactivate QR
    Sistema-->>Interface: 1.8 codigoEQRCode
    deactivate Sistema
    Interface-->>Aluno: 1.9 exibirCupomUnicoEQRCode
```

