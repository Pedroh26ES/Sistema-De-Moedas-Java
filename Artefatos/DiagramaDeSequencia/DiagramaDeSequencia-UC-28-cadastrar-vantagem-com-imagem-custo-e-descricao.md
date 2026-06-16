# DiagramaDeSequencia - UC-28 - Cadastrar vantagem com imagem, custo e descricao

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-04-empresa-parceira.md)

```mermaid
sequenceDiagram
    actor Empresa
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant Notificacao as Sistema de Notificacao
    participant Fila as RabbitMQ

    Note over Empresa,DB: 2. Cadastrar vantagem com imagem, custo e descricao
    Empresa->>Interface: 2.1 preencherNovaVantagem(dados)
    Interface->>Sistema: 2.2 cadastrarVantagem(empresa, dados)
    activate Sistema
    Note right of Sistema: Valida titulo, descricao, custo, imagem e empresa dona
    alt Dados obrigatorios ausentes
        Sistema-->>Interface: 2.3 cadastroNegado(motivo)
        Interface-->>Empresa: 2.4 mensagemDeErro
    else Dados validos
        Sistema->>DB: 2.5 salvarVantagem(dados)
        activate DB
        DB-->>Sistema: 2.6 vantagemSalva
        deactivate DB
        Sistema-->>Interface: 2.7 vantagemCriada
        Interface-->>Empresa: 2.8 catalogoAtualizado
    end
    deactivate Sistema
```

