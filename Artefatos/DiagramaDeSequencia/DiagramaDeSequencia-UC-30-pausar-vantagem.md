# DiagramaDeSequencia - UC-30 - Pausar vantagem

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

    Note over Empresa,Fila: 4. Pausar vantagem
    Empresa->>Interface: 4.1 solicitarPausaVantagem(id)
    Interface->>Sistema: 4.2 pausarVantagem(empresa, id)
    activate Sistema
    Sistema->>DB: 4.3 validarVantagemEConsultarCuponsPendentes(id)
    activate DB
    DB-->>Sistema: 4.4 vantagemECuponsPendentes
    Sistema->>DB: 4.5 marcarVantagemComoPausada(id)
    DB-->>Sistema: 4.6 statusAtualizado
    deactivate DB
    loop Para cada aluno com cupom pendente
        Sistema->>Notificacao: 4.7 avisarCupomPausado(aluno, cupom)
    end
    Sistema->>Fila: 4.8 publicarCUPOM_DESATIVADO()
    Sistema-->>Interface: 4.9 pausaConcluida
    deactivate Sistema
    Interface-->>Empresa: 4.10 statusPausadoExibido
```

