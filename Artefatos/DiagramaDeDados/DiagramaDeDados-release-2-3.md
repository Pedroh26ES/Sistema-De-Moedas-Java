# DiagramaDeDados - release 2-3

Artefato das Releases 2 e 3 do Valoriza Ae.

Este diagrama mostra as entidades persistidas e as relacoes afetadas pelas funcionalidades das Releases 2 e 3.

## Diagrama de dados

```mermaid
erDiagram
    USUARIOS {
        long id PK
        string nome
        string email UK
        string senha_hash
        string perfil
        boolean ativo
    }

    ALUNOS {
        long id PK,FK
        string cpf UK
        string rg
        string endereco
        string curso
        long instituicao_id FK
        int saldo_moedas
    }

    PROFESSORES {
        long id PK,FK
        string cpf UK
        string departamento
        long instituicao_id FK
        int saldo_moedas
        string ultimo_credito_semestral
    }

    EMPRESAS_PARCEIRAS {
        long id PK,FK
        string cnpj UK
        string endereco
        string contato
    }

    INSTITUICOES {
        long id PK
        string nome UK
        string cidade
    }

    CURSOS {
        long id PK
        string nome
        long instituicao_id FK
    }

    VANTAGENS {
        long id PK
        string titulo
        string descricao
        string foto_url
        int custo_moedas
        boolean ativa
        long empresa_id FK
    }

    TRANSACOES {
        long id PK
        string tipo
        int valor
        datetime criada_em
        string mensagem
        string codigo_cupom
        boolean cupom_validado
        datetime validado_em
        long professor_id FK
        long aluno_id FK
        long empresa_id FK
        long vantagem_id FK
    }

    EMAILS_NOTIFICACAO {
        long id PK
        string destinatario
        string assunto
        string conteudo
        string codigo_referencia
        datetime criado_em
    }

    REDEFINICOES_SENHA {
        long id PK
        long usuario_id FK
        string token_hash
        datetime expira_em
        boolean usado
        datetime criado_em
    }

    EVENTOS_FILA_LOCAL {
        long id PK
        string tipo
        string payload
        string origem
        datetime criado_em
    }

    USUARIOS ||--o| ALUNOS : especializa
    USUARIOS ||--o| PROFESSORES : especializa
    USUARIOS ||--o| EMPRESAS_PARCEIRAS : especializa
    USUARIOS ||--o{ REDEFINICOES_SENHA : solicita

    INSTITUICOES ||--o{ ALUNOS : matricula
    INSTITUICOES ||--o{ PROFESSORES : vincula
    INSTITUICOES ||--o{ CURSOS : oferece

    EMPRESAS_PARCEIRAS ||--o{ VANTAGENS : cadastra
    ALUNOS ||--o{ TRANSACOES : possui
    PROFESSORES ||--o{ TRANSACOES : envia
    EMPRESAS_PARCEIRAS ||--o{ TRANSACOES : recebe_validacao
    VANTAGENS ||--o{ TRANSACOES : gera_resgate
```

## Regras representadas

- usuarios concentra login, senha, perfil e status ativo.
- alunos, professores e empresas_parceiras especializam usuarios por heranca JPA joined.
- instituicoes e cursos sao pre-cadastrados; o aluno escolhe uma instituicao e um curso valido para ela.
- professores ficam vinculados a uma instituicao e possuem cota semestral em saldo_moedas.
- vantagens pertencem a uma empresa e possuem status ativo/inativo.
- transacoes registra credito semestral, envio de moedas e resgate de vantagem com cupom.
- codigo_cupom, cupom_validado e validado_em controlam o ciclo do cupom.
- emails_notificacao registra notificacoes internas e emails enviados pelo EmailJS.
- redefinicoes_senha guarda tokens de recuperacao por hash e validade.
- eventos_fila_local preserva rastreabilidade quando o fallback local da fila esta ativo em desenvolvimento.
