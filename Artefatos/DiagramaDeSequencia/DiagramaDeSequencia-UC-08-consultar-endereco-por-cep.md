# DiagramaDeSequencia - UC-08 - Consultar endereco por CEP

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-01-conta-cadastro-seguranca.md)

```mermaid
sequenceDiagram
    actor Usuario
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados
    participant ViaCEP

    Note over Usuario,ViaCEP: 4. Consultar endereco por CEP
    Usuario->>Interface: 4.1 informarCep(cep)
    Interface->>Sistema: 4.2 consultarCep(cep)
    activate Sistema
    Sistema->>ViaCEP: 4.3 buscarEndereco(cep)
    activate ViaCEP
    ViaCEP-->>Sistema: 4.4 respostaViaCep
    deactivate ViaCEP
    alt CEP encontrado
        Sistema-->>Interface: 4.5 enderecoFormatado
        Interface-->>Usuario: 4.6 preencherEndereco(endereco)
    else CEP invalido ou nao encontrado
        Sistema-->>Interface: 4.7 erroConsultaCep
        Interface-->>Usuario: 4.8 informarCepInvalido()
    end
    deactivate Sistema
```

