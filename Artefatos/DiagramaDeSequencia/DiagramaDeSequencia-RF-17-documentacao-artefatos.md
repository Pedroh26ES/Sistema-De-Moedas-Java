# DiagramaDeSequencia - RF-17 - Manter diagramas e artefatos das releases

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Equipe
    participant Repositorio as Repositorio do Projeto
    participant Artefatos as Artefatos UML
    participant GitHub

    Note over Equipe,GitHub: 1. Manutencao dos artefatos das Releases 2 e 3
    Equipe->>Repositorio: 1.1 atualizarArtefato(tipoDiagrama)
    activate Repositorio
    Repositorio->>Artefatos: 1.2 salvarDiagrama(casos, componentes, dados, sequencia, comunicacao, implantacao)
    activate Artefatos
    Artefatos-->>Repositorio: 1.3 artefatoAtualizado
    deactivate Artefatos
    Repositorio->>GitHub: 1.4 publicarAlteracoes()
    activate GitHub
    GitHub-->>Repositorio: 1.5 renderizacaoMarkdownMermaid
    deactivate GitHub
    alt Renderizacao correta
        Repositorio-->>Equipe: 1.6 artefatoDisponivelParaEntrega
    else Falha de renderizacao
        Repositorio-->>Equipe: 1.7 revisarSintaxeOuDividirArquivo
    end
    deactivate Repositorio
```

