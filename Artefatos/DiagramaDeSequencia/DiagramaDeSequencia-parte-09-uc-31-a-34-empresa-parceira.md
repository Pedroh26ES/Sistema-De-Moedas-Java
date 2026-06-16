# DiagramaDeSequencia - Empresa parceira - UC-31 a UC-34

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados, notas de regra e fragmentos `alt`/`loop`.

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

    Note over Empresa,DB: 2. Excluir vantagem sem historico de cupom
    Empresa->>Interface: 2.1 solicitarExclusaoVantagem(id)
    Interface->>Sistema: 2.2 excluirVantagem(empresa, id)
    activate Sistema
    Sistema->>DB: 2.3 verificarHistoricoDeResgates(id)
    activate DB
    DB-->>Sistema: 2.4 historicoDaVantagem
    alt Sem historico de cupom
        Sistema->>DB: 2.5 removerVantagem(id)
        DB-->>Sistema: 2.6 vantagemRemovida
        Sistema-->>Interface: 2.7 exclusaoConcluida
        Interface-->>Empresa: 2.8 vantagemRemovidaDaLista
    else Possui cupom ou resgate
        Sistema-->>Interface: 2.9 exclusaoBloqueada
        Interface-->>Empresa: 2.10 "Vantagem possui historico. Use pausar."
    end
    deactivate DB
    deactivate Sistema

    Note over Sistema,Notificacao: 3. Notificar aluno com cupom pendente
    Sistema->>DB: 3.1 consultarCuponsPendentesDaVantagem(id)
    activate DB
    DB-->>Sistema: 3.2 alunosAfetados
    deactivate DB
    loop Para cada cupom pendente
        Sistema->>Notificacao: 3.3 registrarEEnviarAviso(aluno, cupom, status)
        Notificacao-->>Sistema: 3.4 avisoRegistrado
    end

    Note over Empresa,DB: 4. Consultar cupom por codigo
    Empresa->>Interface: 4.1 informarCodigoCupom(codigo)
    Interface->>Sistema: 4.2 consultarCupom(empresa, codigo)
    activate Sistema
    Sistema->>DB: 4.3 buscarCupomPorCodigo(codigo)
    activate DB
    DB-->>Sistema: 4.4 dadosCupom
    alt Cupom encontrado e pertence a empresa
        Sistema-->>Interface: 4.5 detalhesDoCupom
        Interface-->>Empresa: 4.6 exibirDadosParaConferencia
    else Cupom inexistente ou de outra empresa
        Sistema-->>Interface: 4.7 consultaBloqueada
        Interface-->>Empresa: 4.8 "Cupom nao encontrado para esta empresa."
    end
    deactivate DB
    deactivate Sistema
```
