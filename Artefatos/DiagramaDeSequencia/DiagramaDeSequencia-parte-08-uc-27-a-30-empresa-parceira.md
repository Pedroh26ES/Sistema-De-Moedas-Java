# DiagramaDeSequencia - Empresa parceira - UC-27 a UC-30

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

    Note over Empresa,DB: 1. Ver catalogo, cupons e historico
    Empresa->>Interface: 1.1 abrirPainelEmpresa()
    Interface->>Sistema: 1.2 carregarDashboardEmpresa(empresa)
    activate Sistema
    Sistema->>DB: 1.3 buscarCatalogoCuponsHistorico(empresa)
    activate DB
    DB-->>Sistema: 1.4 dadosDaEmpresa
    deactivate DB
    Sistema-->>Interface: 1.5 dashboardEmpresa
    deactivate Sistema
    Interface-->>Empresa: 1.6 exibirCatalogoCuponsHistorico

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

    Note over Empresa,DB: 3. Editar vantagem
    Empresa->>Interface: 3.1 alterarDadosDaVantagem(id, dados)
    Interface->>Sistema: 3.2 editarVantagem(empresa, id, dados)
    activate Sistema
    Sistema->>DB: 3.3 consultarVantagemDaEmpresa(empresa, id)
    activate DB
    DB-->>Sistema: 3.4 vantagemEncontrada
    alt Vantagem pertence a empresa
        Sistema->>DB: 3.5 salvarAlteracoes(dados)
        DB-->>Sistema: 3.6 alteracoesSalvas
        Sistema-->>Interface: 3.7 edicaoConcluida
        Interface-->>Empresa: 3.8 vantagemAtualizada
    else Vantagem nao pertence a empresa
        Sistema-->>Interface: 3.9 edicaoBloqueada
        Interface-->>Empresa: 3.10 "Vantagem nao encontrada para esta empresa."
    end
    deactivate DB
    deactivate Sistema

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
