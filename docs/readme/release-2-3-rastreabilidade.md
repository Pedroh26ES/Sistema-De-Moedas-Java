# Rastreabilidade - Releases 2 e 3

Artefato das Releases 2 e 3 do Valoriza Ae.

## Escopo consolidado

### Release 2

- Cadastro completo de aluno com nome, email, senha, CPF, RG, endereco, instituicao e curso.
- Instituicoes e cursos pre-cadastrados para selecao do aluno.
- Professor pre-cadastrado pela instituicao parceira, com nome, CPF, departamento e instituicao.
- Login por perfil e acesso restrito ao painel correto.
- Cota semestral do professor e envio de moedas com justificativa obrigatoria.
- Extrato do aluno e do professor com filtro por periodo.
- Cadastro, edicao, pausa/publicacao e exclusao segura de vantagens pela empresa parceira.
- Catalogo de vantagens para o aluno com custo, descricao, imagem e instrucao de uso.
- Resgate de vantagem com desconto no saldo e bloqueio de resgate duplicado.
- Emails e notificacoes internas para eventos principais.

### Release 3

- EmailJS para envio real de notificacoes e recuperacao de senha.
- Template unico de email que se adapta ao tipo de evento e ao perfil do destinatario.
- QR Code gerado para cada cupom de vantagem.
- Cupom unico por resgate, consultado por codigo ou QR Code.
- Validacao do cupom pela empresa dona da vantagem.
- Bloqueio de cupom ja validado, cupom de outra empresa ou cupom de vantagem pausada.
- RabbitMQ para publicacao de eventos do sistema.
- Fila local persistida em banco como fallback de desenvolvimento quando o RabbitMQ nao estiver disponivel.
- ViaCEP no cadastro para preencher endereco a partir do CEP.
- Recuperacao de senha por link enviado ao email do usuario.
- Preparacao para deploy com frontend Vercel e backend Render.

---

---

## Matriz de rastreabilidade

| Requisito | Release | Onde aparece no diagrama | Principal implementacao |
| --- | --- | --- | --- |
| Cadastro de aluno com instituicao e curso | 2 | Cadastro com ViaCEP, dados impactados | `CadastroService`, `ApiController`, `Instituicao`, `Curso`, `Aluno` |
| Professor pre-cadastrado e vinculado a instituicao | 2 | Casos de uso, dados impactados | `DadosIniciais`, `Professor`, `MoedaService` |
| Envio de moedas com justificativa | 2 | Sequencia de envio de moedas | `MoedaService.enviarMoedas` |
| Extrato e notificacoes por perfil | 2 | Componentes, envio, resgate, validacao | `AlunoService`, `MoedaService`, `EmailOutboxGateway` |
| Cadastro e gestao de vantagens | 2 | Casos de uso, pausa/exclusao | `VantagemService.cadastrar`, `atualizar`, `alterarStatus`, `excluir` |
| Resgate sem duplicidade | 2 | Sequencia de resgate | `VantagemService.resgatar` |
| Email real para eventos | 3 | Componentes, envio, recuperacao, resgate | `EmailOutboxGateway`, `EmailTemplateService` |
| Recuperacao de senha por link | 3 | Sequencia de recuperacao de senha | `RecuperacaoSenhaService`, `RedefinicaoSenha` |
| QR Code do cupom | 3 | Sequencia de resgate | `QrCodeService`, endpoint `/api/cupons/{codigo}/qrcode` |
| Validacao de cupom pela empresa | 3 | Sequencia de validacao | `VantagemService.validarCupom` |
| RabbitMQ e rastreabilidade | 3 | Componentes, eventos e implantacao | `RabbitMqFilaService`, `EventoFilaLocal` |
| ViaCEP no cadastro | 3 | Cadastro com ViaCEP | `ViaCepService`, endpoint `/api/cep/{cep}` |

---

---

## Observacoes de arquitetura

- A regra de negocio fica nos services; controllers recebem requisicoes, exigem sessao e retornam DTOs/respostas.
- O acesso ao banco segue ORM com JPA/Hibernate e repositories Panache funcionando como DAO.
- Em ambiente local, o fallback de fila evita travar a demonstracao quando o RabbitMQ nao estiver aberto no terminal.
- Em deploy/producao, o RabbitMQ deve estar disponivel para manter a rastreabilidade completa dos eventos.
- O QR Code nao concede o beneficio sozinho: ele abre o caminho de validacao para a empresa confirmar o atendimento.
