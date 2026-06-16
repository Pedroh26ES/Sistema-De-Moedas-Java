# DiagramaDeSequencia - RF-13 - Consultar ViaCEP para preencher endereco

Artefato das Releases 2 e 3 do Valoriza Ae.

Diagrama de sequencia derivado do requisito funcional correspondente.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant ViaCEP

    Note over Usuario,ViaCEP: 1. Preenchimento automatico de endereco por CEP
    Usuario->>Interface: 1.1 informarCep(cep)
    activate Interface
    Interface->>Sistema: 1.2 consultarEnderecoPorCep(cep)
    activate Sistema
    Sistema->>ViaCEP: 1.3 buscarEndereco(cep)
    activate ViaCEP
    ViaCEP-->>Sistema: 1.4 respostaCep
    deactivate ViaCEP
    alt CEP encontrado
        Sistema-->>Interface: 1.5 enderecoFormatado
        Interface-->>Usuario: 1.6 preencherEnderecoAutomaticamente
    else CEP invalido ou nao encontrado
        Sistema-->>Interface: 1.7 erroConsultaCep
        Interface-->>Usuario: 1.8 solicitarCorrecaoCep
    end
    deactivate Sistema
    deactivate Interface
```

