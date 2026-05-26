# Valoriza Aê - historias e requisitos expandidos

## Objetivo do produto

Transformar moedas academicas em um ciclo completo: reconhecimento do professor, progresso visivel para o aluno, troca por vantagens reais e validacao presencial pela empresa parceira.

## Historias de usuario

### Aluno

- Como aluno, quero visualizar meu saldo, moedas conquistadas, moedas usadas e cupons validados para entender se minha participacao esta gerando resultado real.
- Como aluno, quero filtrar vantagens por "todas", "posso resgatar" e "metas" para encontrar rapidamente beneficios viaveis.
- Como aluno, quero ver quanto falta para a proxima vantagem para saber qual meta posso perseguir.
- Como aluno, quero receber um cupom ao resgatar uma vantagem para apresentar o codigo na empresa parceira.
- Como aluno, quero acompanhar se meu cupom esta pendente ou validado para saber se a troca foi concluida.

### Professor

- Como professor, quero enviar moedas com valor e justificativa obrigatoria para reconhecer meritos de forma transparente.
- Como professor, quero usar valores rapidos e modelos de justificativa para registrar reconhecimentos com menos atrito.
- Como professor, quero buscar alunos por nome, email ou curso para selecionar o destino correto.
- Como professor, quero visualizar saldos dos alunos para acompanhar a evolucao da turma e decidir novos reconhecimentos.
- Como professor, quero manter historico de envios para auditar minhas distribuicoes de moedas.

### Empresa parceira

- Como empresa parceira, quero cadastrar vantagens com foto, descricao, custo e status para manter meu catalogo atualizado.
- Como empresa parceira, quero ver um preview da vantagem antes de salvar para reduzir erros no catalogo.
- Como empresa parceira, quero consultar e validar cupons pelo codigo para confirmar que a troca presencial aconteceu.
- Como empresa parceira, quero acompanhar cupons pendentes e validados para controlar atendimentos e beneficios entregues.
- Como empresa parceira, quero pausar uma vantagem sem apagar historico para impedir novos resgates enquanto mantenho os cupons ja gerados rastreaveis.
- Como empresa parceira, quero excluir vantagens que ainda nao possuem resgate para manter o catalogo limpo.

## Requisitos funcionais adicionados

- RF-01: O sistema deve exibir o nome Valoriza Aê em paginas, shell React e documentacao.
- RF-02: O sistema deve criar dados iniciais mais ricos: mais alunos, mais empresas parceiras, mais vantagens e resgates iniciais.
- RF-03: O sistema deve armazenar status de validacao do cupom em cada transacao de resgate.
- RF-04: O sistema deve permitir que a empresa valide um cupom somente se ele pertence a ela.
- RF-05: O sistema deve impedir validacao duplicada do mesmo cupom.
- RF-06: O sistema deve registrar notificacao quando um cupom for validado.
- RF-07: O extrato deve mostrar status pendente ou validado para transacoes com cupom.
- RF-08: O painel do aluno deve mostrar progresso, filtros do catalogo e resumo de impacto das moedas.
- RF-09: O painel do professor deve mostrar alunos com saldo e ferramentas de envio mais rapidas.
- RF-10: O painel da empresa deve mostrar cupons pendentes, validados, consulta de cupom e preview de vantagem.
- RF-11: O email de envio de moedas deve ser registrado para aluno e professor.
- RF-12: O email de cupom deve conter codigo, link de validacao e URL do QR Code unico.
- RF-13: O QR Code do cupom deve abrir a tela de validacao da empresa com o codigo preenchido.
- RF-14: O projeto deve conter diagramas de sequencia, comunicacao e implantacao para as Releases 2 e 3.

## Regras de negocio

- Apenas aluno pode resgatar vantagens.
- Apenas empresa dona da vantagem pode validar o cupom gerado por aquele resgate.
- Resgate desconta moedas do aluno imediatamente.
- Validacao de cupom nao altera saldo; ela confirma a entrega do beneficio.
- Cupom validado nao pode ser validado novamente.
- Professor precisa informar justificativa para todo envio de moedas.
- Cota semestral do professor continua acumulavel quando nao usada.

## Fluxo principal

1. Professor recebe ou acumula cota semestral.
2. Professor envia moedas ao aluno com justificativa.
3. Aluno acompanha saldo, nivel e catalogo.
4. Aluno resgata uma vantagem e recebe cupom.
5. Empresa consulta o codigo do cupom.
6. Empresa valida o cupom e o sistema registra a conclusao no historico.
