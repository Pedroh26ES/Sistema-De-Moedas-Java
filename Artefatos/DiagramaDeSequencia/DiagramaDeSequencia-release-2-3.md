# DiagramaDeSequencia - release 2-3

Artefato das Releases 2 e 3 do Valoriza Ae.

Este arquivo funciona como indice dos diagramas de sequencia separados por caso de uso. Cada caso de uso possui seu proprio arquivo com exatamente um diagrama Mermaid.

Os diagramas seguem o modelo do gabarito, com participantes fixos, blocos numerados, mensagens numeradas, retornos tracejados e fragmentos `alt`, `loop` e `opt` quando necessario.

## Grupos

- [Conta, cadastro e seguranca](DiagramaDeSequencia-01-conta-cadastro-seguranca.md) - UC-01 a UC-11.
- [Aluno](DiagramaDeSequencia-02-aluno.md) - UC-12 a UC-19.
- [Professor](DiagramaDeSequencia-03-professor.md) - UC-20 a UC-26.
- [Empresa parceira](DiagramaDeSequencia-04-empresa-parceira.md) - UC-27 a UC-36.
- [Integracoes e rastreabilidade](DiagramaDeSequencia-05-integracoes-rastreabilidade.md) - UC-37 a UC-41.
- [Diagrama completo consolidado](DiagramaDeSequencia-completo-release-2-3.md).

## Casos de uso diretos

- Conta, cadastro e seguranca: [UC-01 - Entrar no sistema](DiagramaDeSequencia-UC-01-entrar-no-sistema.md)
- Conta, cadastro e seguranca: [UC-02 - Identificar usuario logado](DiagramaDeSequencia-UC-02-identificar-usuario-logado.md)
- Conta, cadastro e seguranca: [UC-03 - Sair do sistema](DiagramaDeSequencia-UC-03-sair-do-sistema.md)
- Conta, cadastro e seguranca: [UC-04 - Cadastrar aluno](DiagramaDeSequencia-UC-04-cadastrar-aluno.md)
- Conta, cadastro e seguranca: [UC-05 - Cadastrar empresa parceira](DiagramaDeSequencia-UC-05-cadastrar-empresa-parceira.md)
- Conta, cadastro e seguranca: [UC-06 - Selecionar instituicao pre-cadastrada](DiagramaDeSequencia-UC-06-selecionar-instituicao-pre-cadastrada.md)
- Conta, cadastro e seguranca: [UC-07 - Selecionar curso da instituicao](DiagramaDeSequencia-UC-07-selecionar-curso-da-instituicao.md)
- Conta, cadastro e seguranca: [UC-08 - Consultar endereco por CEP](DiagramaDeSequencia-UC-08-consultar-endereco-por-cep.md)
- Conta, cadastro e seguranca: [UC-09 - Solicitar recuperacao de senha](DiagramaDeSequencia-UC-09-solicitar-recuperacao-de-senha.md)
- Conta, cadastro e seguranca: [UC-10 - Redefinir senha por link](DiagramaDeSequencia-UC-10-redefinir-senha-por-link.md)
- Conta, cadastro e seguranca: [UC-11 - Bloquear acesso fora do perfil](DiagramaDeSequencia-UC-11-bloquear-acesso-fora-do-perfil.md)
- Aluno: [UC-12 - Ver saldo, extrato, notificacoes e cupons](DiagramaDeSequencia-UC-12-ver-saldo-extrato-notificacoes-e-cupons.md)
- Aluno: [UC-13 - Filtrar extrato por periodo](DiagramaDeSequencia-UC-13-filtrar-extrato-por-periodo.md)
- Aluno: [UC-14 - Consultar catalogo de vantagens](DiagramaDeSequencia-UC-14-consultar-catalogo-de-vantagens.md)
- Aluno: [UC-15 - Filtrar vantagens disponiveis, adquiridas e metas](DiagramaDeSequencia-UC-15-filtrar-vantagens-disponiveis-adquiridas-e-metas.md)
- Aluno: [UC-16 - Resgatar vantagem](DiagramaDeSequencia-UC-16-resgatar-vantagem.md)
- Aluno: [UC-17 - Impedir resgate duplicado da mesma vantagem](DiagramaDeSequencia-UC-17-impedir-resgate-duplicado-da-mesma-vantagem.md)
- Aluno: [UC-18 - Ver cupom, status e QR Code](DiagramaDeSequencia-UC-18-ver-cupom-status-e-qr-code.md)
- Aluno: [UC-19 - Ampliar QR Code do cupom](DiagramaDeSequencia-UC-19-ampliar-qr-code-do-cupom.md)
- Professor: [UC-20 - Ver cota, alunos, extrato e notificacoes](DiagramaDeSequencia-UC-20-ver-cota-alunos-extrato-e-notificacoes.md)
- Professor: [UC-21 - Filtrar extrato por periodo](DiagramaDeSequencia-UC-21-filtrar-extrato-por-periodo.md)
- Professor: [UC-22 - Creditar cota semestral](DiagramaDeSequencia-UC-22-creditar-cota-semestral.md)
- Professor: [UC-23 - Buscar e selecionar aluno](DiagramaDeSequencia-UC-23-buscar-e-selecionar-aluno.md)
- Professor: [UC-24 - Enviar moedas com justificativa](DiagramaDeSequencia-UC-24-enviar-moedas-com-justificativa.md)
- Professor: [UC-25 - Validar saldo e motivo do envio](DiagramaDeSequencia-UC-25-validar-saldo-e-motivo-do-envio.md)
- Professor: [UC-26 - Receber confirmacao do envio](DiagramaDeSequencia-UC-26-receber-confirmacao-do-envio.md)
- Empresa parceira: [UC-27 - Ver catalogo, cupons e historico](DiagramaDeSequencia-UC-27-ver-catalogo-cupons-e-historico.md)
- Empresa parceira: [UC-28 - Cadastrar vantagem com imagem, custo e descricao](DiagramaDeSequencia-UC-28-cadastrar-vantagem-com-imagem-custo-e-descricao.md)
- Empresa parceira: [UC-29 - Editar vantagem](DiagramaDeSequencia-UC-29-editar-vantagem.md)
- Empresa parceira: [UC-30 - Pausar vantagem](DiagramaDeSequencia-UC-30-pausar-vantagem.md)
- Empresa parceira: [UC-31 - Reativar vantagem](DiagramaDeSequencia-UC-31-reativar-vantagem.md)
- Empresa parceira: [UC-32 - Excluir vantagem sem historico de cupom](DiagramaDeSequencia-UC-32-excluir-vantagem-sem-historico-de-cupom.md)
- Empresa parceira: [UC-33 - Notificar aluno com cupom pendente](DiagramaDeSequencia-UC-33-notificar-aluno-com-cupom-pendente.md)
- Empresa parceira: [UC-34 - Consultar cupom por codigo](DiagramaDeSequencia-UC-34-consultar-cupom-por-codigo.md)
- Empresa parceira: [UC-35 - Validar cupom do aluno](DiagramaDeSequencia-UC-35-validar-cupom-do-aluno.md)
- Empresa parceira: [UC-36 - Bloquear cupom usado, pausado ou de outra empresa](DiagramaDeSequencia-UC-36-bloquear-cupom-usado-pausado-ou-de-outra-empresa.md)
- Integracoes e rastreabilidade: [UC-37 - Enviar email real e registrar notificacao](DiagramaDeSequencia-UC-37-enviar-email-real-e-registrar-notificacao.md)
- Integracoes e rastreabilidade: [UC-38 - Gerar QR Code do cupom](DiagramaDeSequencia-UC-38-gerar-qr-code-do-cupom.md)
- Integracoes e rastreabilidade: [UC-39 - Publicar evento operacional](DiagramaDeSequencia-UC-39-publicar-evento-operacional.md)
- Integracoes e rastreabilidade: [UC-40 - Persistir evento local em desenvolvimento](DiagramaDeSequencia-UC-40-persistir-evento-local-em-desenvolvimento.md)
- Integracoes e rastreabilidade: [UC-41 - Manter rastreabilidade em extratos e notificacoes](DiagramaDeSequencia-UC-41-manter-rastreabilidade-em-extratos-e-notificacoes.md)

## Cobertura

A divisao preserva os 41 casos de uso do diagrama de casos de uso das Releases 2 e 3:

- Conta, cadastro e seguranca: UC-01 a UC-11.
- Aluno: UC-12 a UC-19.
- Professor: UC-20 a UC-26.
- Empresa parceira: UC-27 a UC-36.
- Integracoes e rastreabilidade: UC-37 a UC-41.

## Observacao

Cada arquivo de caso de uso contem um unico diagrama Mermaid, seguindo a estrutura visual do gabarito.


