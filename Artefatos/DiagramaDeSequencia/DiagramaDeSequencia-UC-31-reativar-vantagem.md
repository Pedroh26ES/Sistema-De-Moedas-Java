# DiagramaDeSequencia - UC-31 - Reativar vantagem

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

    Note over Empresa,Fila: 1. Reativar vantagem
    Empresa->>Interface: 1.1 solicitarReativacaoVantagem(id)
    Interface->>Sistema: 1.2 reativarVantagem(empresa, id)
    activate Sistema
    Sistema->>DB: 1.3 validarVantagemEConsultarCuponsPendentes(id)
    activate DB
    DB-->>Sistema: 1.4 vantagemECuponsPendentes
    Sistema->>DB: 1.5 marcarVantagemComoAtiva(id)
    DB-->>Sistema: 1.6 statusAtualizado
    deactivate DB
    loop Para cada aluno com cupom pendente
        Sistema->>Notificacao: 1.7 avisarCupomDisponivel(aluno, cupom)
    end
    Sistema->>Fila: 1.8 publicarCUPOM_REATIVADO()
    Sistema-->>Interface: 1.9 reativacaoConcluida
    deactivate Sistema
    Interface-->>Empresa: 1.10 statusAtivoExibido
```

