# DiagramaDeSequencia - UC-22 - Creditar cota semestral

Artefato das Releases 2 e 3 do Valoriza Ae.

Modelo baseado no gabarito: participantes fixos, bloco numerado, mensagens numeradas, retornos tracejados e fragmentos UML quando necessario.

[Voltar ao indice geral](DiagramaDeSequencia-release-2-3.md) | [Voltar ao grupo](DiagramaDeSequencia-03-professor.md)

```mermaid
sequenceDiagram
    actor Professor
    participant Interface
    participant Sistema as Sistema Valoriza Ae
    participant DB as Banco de Dados

    Note over Sistema,DB: 3. Creditar cota semestral
    Sistema->>Sistema: 3.1 identificarSemestreAtual()
    Sistema->>DB: 3.2 buscarProfessores()
    activate DB
    DB-->>Sistema: 3.3 professoresEncontrados
    loop Para cada professor
        Sistema->>DB: 3.4 atualizarCotaSemestral(professor)
        DB-->>Sistema: 3.5 cotaAtualizada
        Sistema->>DB: 3.6 registrarCreditoSemestral(professor)
        DB-->>Sistema: 3.7 creditoRegistrado
    end
    deactivate DB
```

